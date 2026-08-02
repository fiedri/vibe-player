export function animateTyping(node: HTMLElement, text: string) {
  (async () => {
    while (true) {
      node.textContent = "";

      for (let i = 0; i < text.length; i++) {
        node.textContent = text.slice(0, i + 1);
        await new Promise((resolve) => setTimeout(resolve, 100));
      }

      await new Promise((resolve) => setTimeout(resolve, 1500));
    }
  })();

  return {
    destroy() {
    }
  };
}
