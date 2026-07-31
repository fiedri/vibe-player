<script lang="ts" generics="T">
  import type { Snippet } from "svelte";

  let {
    items = [],
    columns = 3,
    rowHeight = 160,
    gap = 20,
    overscan = 2,
    children,
  }: {
    items: T[];
    columns?: number;
    rowHeight?: number;
    gap?: number;
    overscan?: number;
    children: Snippet<[T]>;
  } = $props();

  let scrollTop = $state(0);
  let containerHeight = $state(0);

  function setupResize(node: HTMLElement) {
    const update = () => { containerHeight = node.clientHeight; };
    update();
    const ro = new ResizeObserver(update);
    ro.observe(node);
    return { destroy() { ro.disconnect(); } };
  }

  const totalRows = $derived(Math.ceil(items.length / columns));
  const totalHeight = $derived(totalRows * rowHeight);

  const visibleStartRow = $derived(
    Math.max(0, Math.floor(scrollTop / rowHeight) - overscan),
  );
  const visibleEndRow = $derived(
    Math.min(
      totalRows,
      Math.ceil((scrollTop + containerHeight) / rowHeight) + overscan,
    ),
  );

  const visibleRows = $derived(
    Array.from({ length: visibleEndRow - visibleStartRow }, (_, i) => {
      const rowIndex = visibleStartRow + i;
      const start = rowIndex * columns;
      return {
        rowIndex,
        rowItems: items.slice(start, start + columns),
        top: rowIndex * rowHeight,
      };
    }),
  );

  function handleScroll(e: Event) {
    const target = e.currentTarget as HTMLElement;
    scrollTop = target.scrollTop;
  }
</script>

<div
  use:setupResize
  onscroll={handleScroll}
  class="h-full w-full overflow-y-auto contain-strict"
>
  <div style="height: {totalHeight}px; width: 100%; position: relative;">
    {#each visibleRows as { rowIndex, rowItems, top } (rowIndex)}
      <div
        style="position: absolute; top: 0; left: 0; width: 100%; height: {rowHeight}px; transform: translateY({top}px); display: grid; grid-template-columns: repeat({columns}, 1fr); gap: {gap}px; place-items: center;"
      >
        {#each rowItems as item, colIndex (rowIndex * columns + colIndex)}
          {@render children(item)}
        {/each}
      </div>
    {/each}
  </div>
</div>
