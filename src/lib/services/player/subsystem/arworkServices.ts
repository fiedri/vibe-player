import { DEFAULT_COVER_DATA_URL, artworkCache, ensureThumbnail } from "$lib/services/artworks";
export class ArtworkService{
  public pendingArtwork = new Map<string, Promise<string>>();
 public async getArtworkSrc(image?: string): Promise<string> {
    if (!image) return DEFAULT_COVER_DATA_URL;

    const cached = artworkCache.get(image);
    if (cached) return cached;

    const pending = this.pendingArtwork.get(image);
    if (pending) return pending;

    const task = ensureThumbnail(image)
      .then((base64) => {
        // ensureThumbnail ya pobló artworkCache (caché compartida). Si
        // devolvió null, NUNCA devolver una URL http(s):// aquí: el plugin
        // la fetchea con HttpURLConnection síncrono sin timeout en el hilo
        // serializado de Capacitor, congelando todos los
        // setMetadata/setPositionState.
        return base64 || DEFAULT_COVER_DATA_URL;
      })
      .finally(() => this.pendingArtwork.delete(image));

    this.pendingArtwork.set(image, task);
    return task;
  }
  /**
   * Precalienta el artwork adyacente (siguiente/anterior) durante el idle.
   * Fire-and-forget: nunca se espera en el hot path de setSong.
   */
  public prewarmArtworkAdyacente(
    previous: string | null,
    next: string | null,
    current?: string | null | undefined,
  ) {

    const siguiente = next
    const anterior = previous
    const actual = current

    const aPrecalentar = [siguiente, anterior].filter(
      (img): img is string => !!img && img !== actual && !artworkCache.has(img),
    );
    if (aPrecalentar.length === 0) return;

    const precalentar = () => {
      for (const img of aPrecalentar) {
        void ensureThumbnail(img).catch(() => {});
      }
    };

    // requestIdleCallback con fallback a setTimeout para WebViews viejas.
    if (typeof requestIdleCallback === "function") {
      requestIdleCallback(precalentar, { timeout: 2000 });
    } else {
      setTimeout(precalentar, 250);
    }
  }
}
