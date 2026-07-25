<script lang="ts">
  import { Pause, Play, SkipBack, SkipForward } from "@lucide/svelte";
   import { player } from "./player.svelte.ts";
   import defaulcover from "./default-cover.png";
  import { Capacitor } from "@capacitor/core";
  let audioElement = $state<HTMLAudioElement | null>(null);

  function formatTime(seconds: number) {
    if (isNaN(seconds)) return "0:00";
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? "0" : ""}${secs}`;
  }
  let showVolumeSlider = $state(false);

  function toggleVolumeSlider() {
    showVolumeSlider = !showVolumeSlider;
  }
  $effect(() => {
    if (!audioElement) return;

    if (player.isPlaying) {
      audioElement.play().catch(() => {
        player.isPlaying = false;
      });
    } else {
      audioElement.pause();
    }
  });
  let progressPercent = $derived(
    player.duration ? (player.currentTime / player.duration) * 100 : 0,
  );
</script>

{#if player.currentSong}
  <audio
    bind:this={audioElement}
    src={Capacitor.convertFileSrc(player.currentSong.audioUrl)}
    bind:currentTime={player.currentTime}
    bind:duration={player.duration}
    bind:volume={player.volume}
  ></audio>
{/if}

<div class="bg-card w-full z-10 overflow-hidden md:hidden">
  <div class="flex items-center gap-2 w-full text-xs text-zinc-400">
    <input
      type="range"
      min="0"
      max={player.duration || 100}
      bind:value={player.currentTime}
      disabled={!player.currentSong}
      style="background: linear-gradient(to right,oklch(0.424 0.199 265.638) {progressPercent}%, #3f3f46 {progressPercent}%);"
      class="w-full h-1 cursor-pointer appearance-none [&::-webkit-slider-thumb]:appearance-none [&::-moz-range-thumb]:appearance-none"
    />
  </div>
  <div class="w-full flex justify-between items-center h-20 px-3">
    <div class="flex items-center gap-3 w-[50%]">
      {#if player.currentSong}
        <img
          src={player.currentSong.image
            ? Capacitor.convertFileSrc(player.currentSong.image)
            : defaulcover}
            loading="lazy"
          alt={player.currentSong.title}
          class="w-14 h-14 border border-border object-cover"
        />
        <div class="flex flex-col overflow-hidden">
          <span class="font-semibold text-sm truncate"
            >{player.currentSong.title}</span
          >
          <span class="text-xs text-muted-foreground truncate"
            >{player.currentSong.artists}</span
          >
        </div>
      {:else}
        <div class="text-xs text-zinc-500">No song selected</div>
      {/if}
    </div>
    <div class="flex items-center justify-between gap-5 *:text-white">
      <button>
        <SkipBack size={18} />
      </button>
      <button
        onclick={() => player.togglePlay()}
        disabled={!player.currentSong}
        class="p-2 hover:scale-105 transition disabled:opacity-50 border border-white p-2"
      >
        {#if player.isPlaying}
          <Pause size={18} />
        {:else}
          <Play class="" size={18} />
        {/if}
      </button>
      <button>
        <SkipForward size={18} />
      </button>
    </div>
  </div>
</div>
