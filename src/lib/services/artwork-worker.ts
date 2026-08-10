
// Decode/resize/encode del artwork FUERA del hilo principal.
// Contrato: recibe { id, base64 } (el artwork ORIGINAL en base64) y responde
// { id, dataUrl } con un data URL JPEG a 512px (dataUrl: null si falló).
// El hilo principal se encarga de Filesystem (leer original / escribir el
// thumbnail); acá solo corre createImageBitmap + OffscreenCanvas + encode.

interface ArtworkWorkerRequest {
  id: number;
  base64: string;
}

interface ArtworkWorkerResponse {
  id: number;
  dataUrl: string | null;
}

const ARTWORK_MAX = 512;

// Este proyecto compila con lib DOM (no webworker), así que el `self` del
// worker no tiene su tipo nativo: tipamos el scope mínimo que usamos.
const scope = self as unknown as {
  onmessage: ((ev: MessageEvent<ArtworkWorkerRequest>) => void) | null;
  postMessage: (message: ArtworkWorkerResponse) => void;
};

function base64ToBlob(base64: string): Blob {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return new Blob([bytes]);
}

function blobToDataUrl(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(blob);
  });
}

scope.onmessage = async (ev) => {
  const { id, base64 } = ev.data;
  try {
    const blob = base64ToBlob(base64);
    const bitmap = await createImageBitmap(blob);
    const scale = Math.min(1, ARTWORK_MAX / Math.max(bitmap.width, bitmap.height));
    const width = Math.max(1, Math.round(bitmap.width * scale));
    const height = Math.max(1, Math.round(bitmap.height * scale));
    const canvas = new OffscreenCanvas(width, height);
    const ctx = canvas.getContext("2d");
    if (!ctx) {
      bitmap.close();
      throw new Error("OffscreenCanvas 2D no disponible");
    }
    ctx.drawImage(bitmap, 0, 0, width, height);
    bitmap.close();
    const jpeg = await canvas.convertToBlob({ type: "image/jpeg", quality: 0.85 });
    const dataUrl = await blobToDataUrl(jpeg);
    scope.postMessage({ id, dataUrl });
  } catch (e) {
    console.warn("artwork-worker:", e);
    scope.postMessage({ id, dataUrl: null });
  }
};
