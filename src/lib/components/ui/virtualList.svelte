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
    itemHeight?: number;
    overscan?: number;
    children: Snippet<[T, number]>;
    scrollTop?: number;
  } = $props();

  let containerHeight = $state(0); // altura visible

  let totalHeight = $derived(items.length * itemHeight);

  let startIndex = $derived(
    Math.max(0, Math.floor(scrollTop / itemHeight) - overscan),
  );

  let endIndex = $derived(
    Math.min(
      items.length,
      Math.ceil((scrollTop + containerHeight) / itemHeight) + overscan,
    ),
  );

  let visibleItems = $derived(
    items.slice(startIndex, endIndex).map((item, index) => ({
      item,
      index: startIndex + index,
      top: (startIndex + index) * itemHeight,
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
    {#each visibleItems as { item, index, top } (index)}
      <div
        style="position: absolute; top: 0; left: 0; width: 100%; height: {itemHeight}px; transform: translateY({top}px);"
      >
        {@render children(item, index)}
      </div>
    {/each}
  </div>
</div>
