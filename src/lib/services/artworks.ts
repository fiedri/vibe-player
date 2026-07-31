import { Directory, Filesystem } from "@capacitor/filesystem";
import defaultCoverUrl from "$lib/assets/default-cover.png?inline";

const ARTWORK_MAX = 512;

export const DEFAULT_COVER_DATA_URL: string = defaultCoverUrl;

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

async function blobToArtwork(blob: Blob): Promise<string> {
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
  const isPng = blob.type.includes("png");
  return canvas.toDataURL(isPng ? "image/png" : "image/jpeg", 0.85);
}

async function readOriginalAsBlob(image: string): Promise<Blob | null> {
  try {
    if (/^https?:\/\//.test(image)) return null;

    if (image.startsWith("/")) {
      const res = await fetch(image);
      if (!res.ok) return null;
      return await res.blob();
    }

    const path = image.startsWith("file://") ? image.slice(7) : image;
    const result = await Filesystem.readFile({ path });
    return typeof result.data === "string" ? base64ToBlob(result.data) : result.data;
  } catch (e) {
    console.warn("No se pudo leer el artwork original:", e);
    return null;
  }
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
  const blob = await readOriginalAsBlob(image);
  if (!blob) return null;

  const dataUrl = await blobToArtwork(blob);
  const base64 = dataUrl.split(",")[1];
  await Filesystem.writeFile({
    path: thumbnailPathFor(image),
    data: base64,
    directory: Directory.Cache,
    recursive: true,
  });
  return dataUrl;
}

export async function ensureThumbnail(image: string): Promise<string | null> {
  if (image === "/default-cover.png") return DEFAULT_COVER_DATA_URL;

  const pending = pendingThumbnails.get(image);
  if (pending) return pending;

  const task = (async () => {
    const existing = await readThumbnail(image);
    if (existing) return existing;
    return generateThumbnail(image);
  })().finally(() => pendingThumbnails.delete(image));

  pendingThumbnails.set(image, task);
  return task;
}
