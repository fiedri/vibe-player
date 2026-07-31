import { Directory, Filesystem } from "@capacitor/filesystem";
import defaultCoverUrl from "$lib/assets/default-cover.png?inline";

const ARTWORK_MAX = 512;

export const DEFAULT_COVER_DATA_URL: string = defaultCoverUrl;

/**
 * Caché en memoria COMPARTIDA: imagen original -> data URL del thumbnail.
 * La llenan ensureThumbnail() (y por lo tanto procesarThumbnails() y el
 * prewarm del player) cada vez que un thumbnail se lee o se genera. El
 * player la consulta primero (getArtworkSrc) para NO repetir lecturas
 * nativas por cada cambio de canción.
 */
export const artworkCache = new Map<string, string>();

const pendingThumbnails = new Map<string, Promise<string | null>>();

export function hashString(input: string): string {
  let hash = 5381;
  for (let i = 0; i < input.length; i++) {
    hash = ((hash << 5) + hash + input.charCodeAt(i)) | 0;
  }
  return (hash >>> 0).toString(16).padStart(8, "0");
}

export function thumbnailPathFor(image: string): string {
  return `art/${hashString(image)}.jpg`;
}

function base64ToBlob(base64: string): Blob {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return new Blob([bytes]);
}

// ---------------------------------------------------------------------------
// Web Worker: decode/resize/encode (createImageBitmap + OffscreenCanvas)
// FUERA del hilo principal. El hilo principal comparte hilo con los calls
// nativos serializados de Capacitor, así que mover el trabajo pesado de
// imagen a un worker desbloquea el MediaSession durante el cambio de tema.
// El contrato es el mismo que el fallback: base64 del original -> data URL
// JPEG a 512px. Solo el decode/resize/encode corre en el worker; las
// lecturas/escrituras de Filesystem quedan en el main thread.
// ---------------------------------------------------------------------------

let artworkWorker: Worker | null = null;
let nextArtworkRequestId = 0;
const pendingArtworkRequests = new Map<number, (dataUrl: string | null) => void>();

function getArtworkWorker(): Worker | null {
  if (artworkWorker) return artworkWorker;
  // WebViews viejas (minSdk 24) pueden no tener Worker u OffscreenCanvas:
  // en ese caso se usa el fallback main-thread.
  if (typeof Worker === "undefined" || typeof OffscreenCanvas === "undefined") {
    return null;
  }
  try {
    artworkWorker = new Worker(new URL("./artwork-worker.ts", import.meta.url), {
      type: "module",
    });
    artworkWorker.addEventListener(
      "message",
      (ev: MessageEvent<{ id: number; dataUrl: string | null }>) => {
        const { id, dataUrl } = ev.data;
        const resolve = pendingArtworkRequests.get(id);
        if (resolve) {
          pendingArtworkRequests.delete(id);
          resolve(dataUrl);
        }
      },
    );
    artworkWorker.addEventListener("error", () => {
      // Worker roto: liberar pendientes (caen al fallback main-thread) y
      // descartar la instancia para que el próximo intento la recree.
      const pendientes = [...pendingArtworkRequests.values()];
      pendingArtworkRequests.clear();
      for (const resolve of pendientes) resolve(null);
      artworkWorker?.terminate();
      artworkWorker = null;
    });
    return artworkWorker;
  } catch (e) {
    console.warn("No se pudo crear el worker de artworks:", e);
    return null;
  }
}

function requestArtworkEnWorker(worker: Worker, base64: string): Promise<string | null> {
  const id = ++nextArtworkRequestId;
  return new Promise((resolve) => {
    pendingArtworkRequests.set(id, resolve);
    worker.postMessage({ id, base64 });
    // Timeout de seguridad: si el worker se cuelga, no colgamos la
    // notificación; el fallback main-thread toma el control.
    setTimeout(() => {
      if (pendingArtworkRequests.has(id)) {
        pendingArtworkRequests.delete(id);
        resolve(null);
      }
    }, 15_000);
  });
}

async function blobToArtwork(base64: string): Promise<string> {
  // 1) Worker con OffscreenCanvas: decode/resize/encode fuera del hilo
  // principal (el mismo hilo serializado donde corre Capacitor).
  const worker = getArtworkWorker();
  if (worker) {
    try {
      const dataUrl = await requestArtworkEnWorker(worker, base64);
      if (dataUrl) return dataUrl;
    } catch (e) {
      console.warn("Worker de artwork falló, uso fallback main-thread:", e);
    }
  }

  // 2) Fallback main-thread (sin Worker/OffscreenCanvas o worker roto).
  const blob = base64ToBlob(base64);
  const bitmap = await createImageBitmap(blob);
  const scale = Math.min(1, ARTWORK_MAX / Math.max(bitmap.width, bitmap.height));
  const width = Math.max(1, Math.round(bitmap.width * scale));
  const height = Math.max(1, Math.round(bitmap.height * scale));
  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  const ctx = canvas.getContext("2d");
  if (!ctx) {
    bitmap.close();
    throw new Error("Canvas 2D no disponible");
  }
  ctx.drawImage(bitmap, 0, 0, width, height);
  bitmap.close();
  // Siempre JPEG a 512px, igual que el worker: consistente con el label del
  // archivo en disco (.jpg). Los PNG se re-encodifican a JPEG sin pérdida
  // perceptible a este tamaño.
  return canvas.toDataURL("image/jpeg", 0.85);
}

async function leerArtworkOriginal(image: string): Promise<string | null> {
  try {
    if (/^https?:\/\//.test(image)) return null;

    if (image.startsWith("/")) {
      const res = await fetch(image);
      if (!res.ok) return null;
      return await blobToBase64(await res.blob());
    }

    const path = image.startsWith("file://") ? image.slice(7) : image;
    const result = await Filesystem.readFile({ path });
    if (typeof result.data === "string") return result.data;
    return await blobToBase64(result.data);
  } catch (e) {
    console.warn("No se pudo leer el artwork original:", e);
    return null;
  }
}

function blobToBase64(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      resolve(result.includes(",") ? result.split(",")[1] : result);
    };
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(blob);
  });
}

async function readThumbnail(image: string): Promise<string | null> {
  try {
    const result = await Filesystem.readFile({
      path: thumbnailPathFor(image),
      directory: Directory.Cache,
    });
    if (typeof result.data !== "string") return null;
    return `data:image/jpeg;base64,${result.data}`;
  } catch {
    return null;
  }
}

async function generateThumbnail(image: string): Promise<string | null> {
  const base64 = await leerArtworkOriginal(image);
  if (!base64) return null;

  const dataUrl = await blobToArtwork(base64);
  const base64Data = dataUrl.split(",")[1];
  await Filesystem.writeFile({
    path: thumbnailPathFor(image),
    data: base64Data,
    directory: Directory.Cache,
    recursive: true,
  });
  return dataUrl;
}

export async function ensureThumbnail(image: string): Promise<string | null> {
  if (image === "/default-cover.png") return DEFAULT_COVER_DATA_URL;

  // Caché en memoria primero: evita lecturas nativas y re-decode cuando el
  // thumbnail ya se procesó en esta sesión.
  const enMemoria = artworkCache.get(image);
  if (enMemoria) return enMemoria;

  const pending = pendingThumbnails.get(image);
  if (pending) return pending;

  const task = (async () => {
    const existing = await readThumbnail(image);
    if (existing) {
      artworkCache.set(image, existing);
      return existing;
    }
    const generated = await generateThumbnail(image);
    if (generated) artworkCache.set(image, generated);
    return generated;
  })().finally(() => pendingThumbnails.delete(image));

  pendingThumbnails.set(image, task);
  return task;
}
