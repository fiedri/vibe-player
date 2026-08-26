import { FlashFilled } from "carbon-icons-svelte";

export function portal(node: HTMLElement) {
  document.body.appendChild(node);
  return {
    destroy() {
      if (node.parentNode) {
        node.parentNode.removeChild(node);
      }
    },
  };
}
interface ExpandOptions {
  onStart?: () => void;
  onEnd?: () => void;
}

export function expand(node: HTMLElement, options: ExpandOptions = {}) {
  let dragging = false;
  let startY = 0;
  let initialHeight = 0;
  let wasPointerDown = false;
  const DRAG_THRESHOLD = 8;

  const handle = node.querySelector("[data-drag-handle]") || node;

  function pointerDown(e: PointerEvent) {
    if (!e.isPrimary) return;

    wasPointerDown = true;
    dragging = false;
    startY = e.clientY;
    initialHeight = node.offsetHeight;

    (handle as HTMLElement).setPointerCapture(e.pointerId);
  }

  function pointerMove(e: PointerEvent) {
    if (!wasPointerDown) return;

    const deltaY = startY - e.clientY;

    if (!dragging && Math.abs(deltaY) > DRAG_THRESHOLD) {
      dragging = true;
      options.onStart?.();
    }

    if (!dragging) return;

    const newHeight = Math.max(74, initialHeight + deltaY);

    if (deltaY < -150) {
      setTransition(true);
      node.style.height = "74px";
      return;
    }

    if (newHeight > (window.innerHeight * 50) / 100) {
      setTransition(true);
      node.style.height = `95%`;
      return;
    }

    node.style.height = `${newHeight}px`;
  }

  function pointerUp(e: PointerEvent) {
    if (!wasPointerDown) return;

    if (dragging) {
      dragging = false;
      if (node.offsetHeight < (window.innerHeight * 50) / 100) {
        setTransition(true);
        node.style.height = `74px`;
      }
    } else {
      e.stopPropagation();
      options.onStart?.();
      setTransition(true);

      const isExpanded = node.offsetHeight > (window.innerHeight * 40) / 100;
      node.style.height = isExpanded ? `74px` : `95%`;
    }

    try {
      (handle as HTMLElement).releasePointerCapture(e.pointerId);
    } catch {}

    wasPointerDown = false;
  }

  function setTransition(active: boolean) {
    if (active) {
      node.classList.add("transition-all", "duration-300", "ease-out");
      setTimeout(() => {
        node.classList.remove("transition-all", "duration-300", "ease-out");
        options.onEnd?.();
      }, 300);
    }
  }

  handle.addEventListener("pointerdown", pointerDown as EventListener);
  window.addEventListener("pointermove", pointerMove as EventListener);
  window.addEventListener("pointerup", pointerUp as EventListener);

  return {
    destroy() {
      handle.removeEventListener("pointerdown", pointerDown as EventListener);
      window.removeEventListener("pointermove", pointerMove as EventListener);
      window.removeEventListener("pointerup", pointerUp as EventListener);
    },
  };
}
