/**
 * Safe clipboard writing utility that works across:
 * 1. Capacitor Clipboard native plugin (if available/installed)
 * 2. Standard navigator.clipboard API (in Secure Contexts: HTTPS, localhost)
 * 3. Fallback textarea + document.execCommand('copy') (for non-secure HTTP dev server over local IP / Android WebView)
 */
export async function copyToClipboard(text: string): Promise<void> {
  // 1. Try Capacitor Clipboard plugin if available on window/global object
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const capClipboard = (globalThis as Record<string, any>).Capacitor?.Plugins?.Clipboard;
  if (capClipboard && typeof capClipboard.write === "function") {
    try {
      await capClipboard.write({ string: text });
      return;
    } catch (e) {
      console.warn("Capacitor Clipboard plugin failed, falling back:", e);
    }
  }

  // 2. Try standard Navigator Clipboard API (Secure Contexts)
  if (typeof navigator !== "undefined" && navigator.clipboard && typeof navigator.clipboard.writeText === "function") {
    try {
      await navigator.clipboard.writeText(text);
      return;
    } catch (e) {
      console.warn("navigator.clipboard.writeText failed, falling back to execCommand:", e);
    }
  }

  // 3. Fallback for non-secure HTTP contexts (e.g. http://192.168.0.103:5173/ on Android WebView / mobile)
  if (typeof document !== "undefined") {
    const textArea = document.createElement("textarea");
    textArea.value = text;

    // Prevent scrolling or zooming in mobile browsers
    textArea.style.position = "fixed";
    textArea.style.top = "0";
    textArea.style.left = "0";
    textArea.style.width = "2em";
    textArea.style.height = "2em";
    textArea.style.padding = "0";
    textArea.style.border = "none";
    textArea.style.outline = "none";
    textArea.style.boxShadow = "none";
    textArea.style.background = "transparent";
    textArea.style.opacity = "0";

    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
      const successful = document.execCommand("copy");
      document.body.removeChild(textArea);
      if (successful) return;
    } catch (err) {
      document.body.removeChild(textArea);
      throw new Error(err instanceof Error ? err.message : "Error al copiar con execCommand");
    }
  }

  throw new Error("El entorno actual no soporta copiar al portapapeles.");
}
