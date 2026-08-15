<script lang="ts" generics="T">
  import type { Snippet } from "svelte";

  let {
    items = [],
    itemHeight = 56,
    overscan = 5,
    children,
    scrollTop = $bindable(0), // ← lo movemos a prop bindable
  }: {
    items: T[];
    itemHeight?: number | ((index: number) => number);
    overscan?: number;
    children: Snippet<[T, number]>;
    scrollTop?: number;
  } = $props();

  let containerHeight = $state(0); // altura visible

  let getItemHeight = $derived(
    typeof itemHeight === "function" ? itemHeight : () => itemHeight,
  );

  let offsets = $derived.by(() => {
    const offs = new Array<number>(items.length + 1);
    offs[0] = 0;
    for (let i = 0; i < items.length; i++) {
      offs[i + 1] = offs[i] + getItemHeight(i);
    }
    return offs;
  });

  let totalHeight = $derived(offsets[items.length] ?? 0);

  let startIndex = $derived(buscarInicio(scrollTop));
  let endIndex = $derived(buscarFin(scrollTop));

  function buscarInicio(scroll: number): number {
    let low = 0;
    let high = items.length;
    while (low < high) {
      const mid = (low + high) >> 1;
      if (offsets[mid] + getItemHeight(mid) <= scroll) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }
    return Math.max(0, low - overscan);
  }

  function buscarFin(scroll: number): number {
    const limite = scroll + containerHeight;
    let low = 0;
    let high = items.length;
    while (low < high) {
      const mid = (low + high) >> 1;
      if (offsets[mid] <= limite) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }
    return Math.min(items.length, low + overscan);
  }

  let visibleItems = $derived(
    items.slice(startIndex, endIndex).map((item, index) => ({
      item,
      index: startIndex + index,
      top: offsets[startIndex + index],
      height: getItemHeight(startIndex + index),
    })),
  );

  function handleScroll(e: Event) {
    const target = e.currentTarget as HTMLElement;
    scrollTop = target.scrollTop;
  }

  function setupResize(node: HTMLElement) {
    const updateHeight = () => {
      containerHeight = node.clientHeight;
    };
    updateHeight();
    const observer = new ResizeObserver(updateHeight);
    observer.observe(node);
    return {
      destroy() {
        observer.disconnect();
      },
    };
  }
</script>

<div
  use:setupResize
  onscroll={handleScroll}
  class="h-full w-full overflow-y-auto contain-strict"
>
  <div style="height: {totalHeight}px; width: 100%; position: relative;">
    {#each visibleItems as { item, index, top, height } (index)}
      <div
        style="position: absolute; top: 0; left: 0; width: 100%; height: {height}px; transform: translateY({top}px);"
      >
        {@render children(item, index)}
      </div>
    {/each}
  </div>
</div>
