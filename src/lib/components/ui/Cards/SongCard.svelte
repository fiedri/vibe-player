<script lang="ts">
      import { player, type Song } from "../player/player.svelte.ts";
      import { Play, Pause, EllipsisVertical } from "@lucide/svelte";

      interface Props {
        song: Song;
        idx: number;
      }

      let { song, idx }: Props = $props();

      let isCurrent = $derived(
        player.currentSong?.id === song.id &&
          player.currentSong?.albumId === song.albumId
      );
      let isPlayingThis = $derived(isCurrent && player.isPlaying);

      function handlePlay() {
        if (isCurrent) {
          player.togglePlay();
        } else {
          player.setSong(song);
        }
      }
    </script>

    <div
      onclick={handlePlay}
      onkeydown={(e) => (e.key === "Enter" || e.key === " ") && handlePlay()}
      role="button"
      tabindex="0"
      class="flex items-center justify-between p-2 hover:bg-zinc-800/50 group text-sm cursor-pointer select-none {isPlayingThis ?
  'bg-card' : ''} transition-all duration-200 ease-in-out gap-2"
    >
      <div class="flex items-center gap-4 flex-1 min-w-0 pointer-events-none">
        <button
          onclick={(e) => {
            e.stopPropagation();
            handlePlay();
          }}
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
            {song.title}
          </p>
          <p class="text-xs text-muted-foreground hover:underline truncate">
            {song.artists}
          </p>
        </div>
      </div>

      <div class="flex gap-2 items-center justify-center shrink-0">
        <span class="text-muted-foreground">{song.duration}</span>
        <button
          onclick={(e) => e.stopPropagation()}
          aria-label="Opciones de canción"
          class="p-1 hover:text-white"
        >
          <EllipsisVertical class="size-4" />
        </button>
      </div>
    </div>
