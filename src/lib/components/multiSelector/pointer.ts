import { selection } from "./selectionStore.svelte";
export function longPress(node: HTMLElement, id: string | number) {
  const duration = 500;
  let timer;
  node.addEventListener("pointerdown", (event) => {
    timer = setTimeout(() => {
      // lo que va a pasar al finalizar el contador
      console.log("se ejecuta el longPress");
      selection.isActive = true
    }, 500);
  });
  const cancelEvents = ["pointerup", "pointercancel"];
  cancelEvents.forEach((event) => {
    node.addEventListener(event, () => {
      if (timer) {
        clearTimeout(timer);
      }
    });
  });

  node.addEventListener("pointermove", () => {
    if (timer) {
      clearTimeout(timer);
    }
  });
}
