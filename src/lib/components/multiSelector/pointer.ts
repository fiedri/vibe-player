import { selection } from "./selectionStore.svelte";
export function longPress(node: HTMLElement, id: string | number) {
  const duration = 500;
  const SLOP = 10;
  let timer;
  let startX = 0;
  let startY = 0;
  let longPressTriggered = false;
  node.classList.add("long-press-item");

  const handlePointerDown = (e: Event) => {
    const pe = e as PointerEvent;
    startX = pe.clientX;
    startY = pe.clientY;
    longPressTriggered = false;
    timer = setTimeout(() => {
      longPressTriggered = true;
      selection.isActive = true;
      selection.toggleId(id);
    }, duration);
  };

  const limpiarContador = () => {
    if (timer) {
      clearTimeout(timer);
    }
  };

  const handlePointerMove = (e: Event) => {
    const pe = e as PointerEvent;
    const dx = pe.clientX - startX;
    const dy = pe.clientY - startY;
    if (Math.hypot(dx, dy) > SLOP) {
      limpiarContador();
    }
  };

  const handleContextMenu = (e: Event) => {
    e.preventDefault();
  };

  const handleClick = (e: Event) => {
    if (longPressTriggered) {
      e.preventDefault();
      e.stopPropagation();
    }
  };

  node.addEventListener("pointerdown", handlePointerDown);
  const cancelEvents = ["pointerup", "pointercancel"];
  cancelEvents.forEach((event) => {
    node.addEventListener(event, limpiarContador);
  });
  node.addEventListener("pointermove", handlePointerMove);
  node.addEventListener("contextmenu", handleContextMenu);
  node.addEventListener("click", handleClick, true);

  return {
    destroy() {
      limpiarContador();
      node.removeEventListener("pointerdown", handlePointerDown);
      node.removeEventListener("pointerup", limpiarContador);
      node.removeEventListener("pointercancel", limpiarContador);
      node.removeEventListener("pointermove", handlePointerMove);
      node.removeEventListener("contextmenu", handleContextMenu);
      node.removeEventListener("click", handleClick, true);
    },
  };
}