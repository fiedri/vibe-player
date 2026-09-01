<script lang="ts">
import { m } from "$lib/paraglide/messages.js";

  import { expand } from "$lib/uiUtils";
  import VirtualList from "../virtualList.svelte";
  import {
    PlayFilledAlt as Play,
    PauseFilled as Pause,
    Draggable,
  } from "carbon-icons-svelte";
  import { displayTitle, displayArtist } from "$lib/types/songs";
  import { formatearMS } from "$lib/utils";

  import { playerService } from "$lib/services/player/PlayerFacade";
  import { fade } from "svelte/transition";
  let heigthContainer = $state(0);
  let isAnimating = $state(false);
  let tight = $derived(heigthContainer > (window.innerHeight * 40) / 100);
  let scrollTop = $state(0);
  let scrollElementList: HTMLElement | null = $state(null);
  function handleAnimationStart() {
    isAnimating = true;
  }

  function handleAnimationEnd() {
    setTimeout(() => {
      isAnimating = false;
    }, 100);
  }
  let dragIndex: number | null = $state(null);
  let dragY = $state(0);
  let pointerOffsetY = 0;
  let dropIndex: number | null = $state(null);

  let rafId: number | null = $state(null);
  const TRIGGER_ZONE = 60;
  function recomputeDropIndex() {
    if (!scrollElementList) return;
    const rect = scrollElementList.getBoundingClientRect();
    const virtualY = scrollTop + (dragY - rect.top);
    dropIndex = Math.min(
      playerService.queue.length - 1,
      Math.max(0, Math.floor(virtualY / ITEM_H)),
    );
  }
  function startAutoScroll() {
    if (dragIndex == null) {
      cancelAnimationFrame(rafId);
      rafId = null;
      return;
    }
    if (scrollElementList) {
      const rect = scrollElementList.getBoundingClientRect();
      const distanceTop = dragY - rect.top;
      const distanceBottom = rect.bottom - dragY;
      if (distanceTop > 0 && distanceTop < TRIGGER_ZONE) {
        const speed = (1 - distanceTop / TRIGGER_ZONE) * 22;
        scrollElementList.scrollTop -= speed;
      } else if (distanceBottom > 0 && distanceBottom < TRIGGER_ZONE) {
        const speed = (1 - distanceBottom / TRIGGER_ZONE) * 22;
        scrollElementList.scrollTop += speed;
      }
      // el dedo puede estar quieto mientras la lista scrollea:
      // el dropIndex tiene que seguir el scroll
      recomputeDropIndex();
    }
    rafId = requestAnimationFrame(startAutoScroll);
  }

  function handleDragStart(e: PointerEvent, idx: null | number) {
    e.preventDefault();
    const el = e.currentTarget as HTMLElement;
    el.setPointerCapture(e.pointerId);
    const row = el.closest("[data-index]") as HTMLElement;
    pointerOffsetY = e.clientY - row.getBoundingClientRect().top;
    dragIndex = idx;
    if (!rafId) {
      rafId = requestAnimationFrame(startAutoScroll);
    }
  }
  function handleDragMove(e: PointerEvent) {
    if (dragIndex === null) return;
    dragY = e.clientY;
    recomputeDropIndex();
  }
  function handleDragEnd(e: PointerEvent) {
    if (dragIndex !== null && dropIndex !== null && dragIndex !== dropIndex) {
      playerService.moveInQueue(dragIndex, dropIndex);
    }
    dragIndex = null;
    dropIndex = null;
    if (rafId) {
      cancelAnimationFrame(rafId);
      rafId = null;
    }
  }
  const ITEM_H = 54;
  function computeShift(idx: number): number {
    if (dropIndex === null || dragIndex === null) return 0;
    const d = dragIndex,
      p = dropIndex;
    if (d < idx && idx <= p) return -ITEM_H;
    if (p <= idx && idx < d) return ITEM_H;
    return 0;
  }
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
  use:expand={{ onStart: handleAnimationStart, onEnd: handleAnimationEnd }}
  class="flex flex-col bg-popover py-2 fixed bottom-0 w-full right-0 left-0 no-copy touch-none"
  bind:offsetHeight={heigthContainer}
  onpointermove={(e) => {
    handleDragMove(e);
  }}
  onpointerup={(e) => handleDragEnd(e)}
  onpointercancel={(e) => handleDragEnd(e)}
>
  <div data-drag-handle class="cursor-pointer select-none py-1">
    <div class="w-20 h-1.5 bg-primary mx-auto mb-2 rounded-full"></div>
    <h2
      class="text-center uppercase font-bold leading-5 p-2 text-muted-foreground text-xs tracking-wider"
    >
     {m["player.queue"]()} 
    </h2>
  </div>

  {#if tight}
    <div
      class="flex-1 min-h-0 w-full border-border border-t {isAnimating
        ? 'pointer-events-none'
        : ''}"
      transition:fade={{ duration: 150 }}
    >
      {#if dragIndex !== null}
        {@const song = playerService.queue[dragIndex]}
        <div
          class="fixed z-50 left-2 right-2 pointer-events-none select-none border border-primary/40 bg-card/95 backdrop-blur-sm shadow-2xl shadow-black/60"
          style="top: 0; transform: translate3d(0, {dragY -
            pointerOffsetY}px, 0) scale(1.02);"
        >
          <div class="flex items-center gap-3 p-2">
            <Draggable size={20} class="text-primary shrink-0" />
            <div class="min-w-0 flex-1">
              <p class="font-medium text-white truncate text-sm">
                {displayTitle(song)}
              </p>
              <p class="text-xs text-muted-foreground truncate">
                {displayArtist(song)}
              </p>
            </div>
            <span class="text-xs text-muted-foreground shrink-0"
              >{typeof song.duration == "number"
                ? formatearMS(song.duration)
                : "00:00"}</span
            >
          </div>
        </div>
      {/if}
      <VirtualList
        bind:scrollTop
        items={playerService.queue}
        itemHeight={54}
        bind:scrollELement={scrollElementList}
      >
        {#snippet children(song, idx)}
          {@const isPlayingThis = playerService.currentSong?.id == song.id}
          {@const shift = computeShift(idx)}
          <!-- svelte-ignore a11y_click_events_have_key_events -->
          <!-- svelte-ignore a11y_no_static_element_interactions -->
          <div
            onclick={() => {
              if (isPlayingThis) {
                playerService.togglePlay();
              } else {
                playerService.setSong(song);
              }
            }}
            data-index={idx}
            role="button"
            tabindex="0"
            class="p-2 hover:bg-card/50 group text-sm cursor-pointer select-none border border-border {isPlayingThis
              ? 'bg-card/50 shadow-[inset_0_0_0_1px_theme(colors.primary)] '
              : ''} {idx === dragIndex ? 'opacity-0' : ''}"
          >
            <div
              class="flex items-center justify-between gap-2 {dragIndex !== null
                ? 'transition-transform duration-150 ease-out'
                : ''}"
              style="transform: translateY({shift}px)"
            >
              <div
                class="flex items-center gap-4 flex-1 min-w-0 pointer-events-none"
              >
                <button
                  aria-label={isPlayingThis ? "Pausar" : "Reproducir"}
                  class="w-6 text-center text-muted-foreground cursor-pointer flex justify-center items-center shrink-0 pointer-events-auto"
                >
                  {#if isPlayingThis}
                    <Pause class="size-4 text-primary fill-current" />
                  {:else}
                    <Play class="size-4 text-white fill-current" />
                  {/if}
                </button>

                <div class="min-w-0 flex-1">
                  <p class="font-medium hover:underline text-white truncate">
                    {displayTitle(song)}
                  </p>
                  <p
                    class="text-xs text-muted-foreground hover:underline truncate"
                  >
                    {displayArtist(song)}
                  </p>
                </div>
              </div>

              <div
                class="relative flex gap-2 items-center justify-center shrink-0"
              >
                <span class="text-muted-foreground"
                  >{typeof song.duration == "number"
                    ? formatearMS(song.duration)
                    : "00:00"}</span
                >
                <button
                  aria-label="Arrastrar para reordenar"
                  class="p-1 hover:text-foreground cursor-grab active:cursor-grabbing touch-none"
                  onpointerdown={(e) => {
                    e.stopPropagation();
                    handleDragStart(e, idx);
                  }}
                  onpointerup={handleDragEnd}
                >
                  <Draggable size={28} />
                </button>
              </div>
            </div>
          </div>
        {/snippet}
      </VirtualList>
    </div>
  {/if}
</div>
