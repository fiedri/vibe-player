<script lang="ts">
  import { onMount } from "svelte";
  import { Pause, Play, ArrowLeft, EllipsisVertical } from "@lucide/svelte";
  import { fly } from "svelte/transition";
  import { cubicOut } from "svelte/easing";
  import {
    PlayFilledAlt,
    SkipBack,
    SkipForward,
    Shuffle,
    Repeat,
    RepeatOne,
    PauseFilled,
    Favorite,
    FavoriteFilled,
  } from "carbon-icons-svelte";
  import { player } from "./playerStore.svelte";
  import defaulcover from "./default-cover.png";
  import { Capacitor } from "@capacitor/core";
  import Button from "../button/button.svelte";
  import MarqueeText from "../wrapper/marqueeText.svelte";

  let audioElement = $state<HTMLAudioElement | null>(null);
  let isSeeking = $state<boolean>(false);
  let seekValue = $state<number>(0);

  onMount(() => {
    player.initMediaSessionHandlers();
  });

  function formatTime(seconds: number) {
    if (isNaN(seconds)) return "0:00";
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? "0" : ""}${secs}`;
  }

  $effect(() => {
    const song = player.currentSong;
    const isPlaying = player.isPlaying;
    if (!audioElement || !song) return;

    if (isPlaying) {
      const playPromise = audioElement.play();
      if (playPromise !== undefined) {
        playPromise.catch((error) => {
          if (error.name !== "AbortError") {
            console.error("Error al reproducir audio:", error);
            // Mutar estado dentro del catch es más seguro usando los métodos de la clase
            player.pause();
          }
        });
      }
    } else {
      audioElement.pause();
    }
  });

  $effect(() => {
    if (audioElement) {
      player.onSeekRequest = (time: number) => {
        if (audioElement) {
          audioElement.currentTime = time;
        }
      };
    }
  });

  let displayTime = $derived(isSeeking ? seekValue : player.currentTime);
  let progressPercent = $derived(
    player.duration ? (displayTime / player.duration) * 100 : 0,
  );

  function handleTimeUpdate() {
    if (!audioElement || isSeeking) return;
    player.currentTime = audioElement.currentTime;
    if (player.isPlaying) {
      player.updatePositionState(player.currentTime, player.duration, false);
    }
  }

  function handleLoadedMetadata() {
    if (!audioElement || !player.currentSong) return;
    player.duration = audioElement.duration;

    // AHORA el audio es real, lanzamos la notificación al sistema
    player.setMetadata(player.currentSong).then(() => {
      player.syncNativePlaybackState(true);
      player.updatePositionState(player.currentTime, player.duration, true);
    });
  }

  function handleSeekChange(e: Event) {
    const target = e.target as HTMLInputElement;
    const newTime = parseFloat(target.value);
    if (audioElement) {
      audioElement.currentTime = newTime;
    }
    player.currentTime = newTime;
    player.updatePositionState(newTime, player.duration, true);
    isSeeking = false;
  }

  function handlePlay() {
    player.isPlaying = true;
    player.syncNativePlaybackState(true);
  }

  function handlePause() {
    player.isPlaying = false;
    player.syncNativePlaybackState(false);
  }

  function handleEnded() {
    player.next();
  }

  function handleError(e: Event) {
    console.error("Error en elemento audio:", e);
    player.isPlaying = false;
  }

  function handleSeekStart(e: Event) {
    isSeeking = true;
    const target = e.target as HTMLInputElement;
    seekValue = parseFloat(target.value);
  }

  function handleSeekInput(e: Event) {
    isSeeking = true;
    const target = e.target as HTMLInputElement;
    seekValue = parseFloat(target.value);
  }

  function handleOpenAndClosePlayer() {
    if (!player.currentSong) return;
    player.isOpened = !player.isOpened;
  }

  function handleNextSong(e?: Event) {
    if (e) e.stopPropagation();
    player.next();
  }

  function handlePreviousSong(e?: MouseEvent | Event) {
    if (e) e.stopPropagation();
    player.previous();
  }
</script>

{#if player.currentSong}
  <audio
    bind:this={audioElement}
    src={player.currentSong?.audioUrl
      ? Capacitor.convertFileSrc(player.currentSong.audioUrl)
      : ""}
    bind:volume={player.volume}
    ontimeupdate={handleTimeUpdate}
    onloadedmetadata={handleLoadedMetadata}
    onplay={handlePlay}
    onpause={handlePause}
    onended={handleEnded}
    onerror={handleError}
  ></audio>
{/if}
{#if !player.isOpened}
  <!-- svelte-ignore a11y_click_events_have_key_events -->
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <div
    class="bg-card w-full z-10 overflow-hidden md:hidden"
    onclick={handleOpenAndClosePlayer}
  >
    <div class="flex items-center gap-2 w-full text-xs text-zinc-400">
      <input
        type="range"
        min="0"
        max={player.duration || 100}
        value={displayTime}
        onpointerdown={handleSeekStart}
        oninput={handleSeekInput}
        onchange={handleSeekChange}
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
            <MarqueeText
              text={player.currentSong?.title}
              class="pr-14 text-white font-black"
            />
            <span class="text-xs text-muted-foreground truncate"
              >{player.currentSong.artists}</span
            >
          </div>
        {:else}
          <div class="text-xs text-zinc-500">No song selected</div>
        {/if}
      </div>
      <div class="flex items-center justify-between gap-5 *:text-white">
        <button
          onclick={handlePreviousSong}
          class="transition-all duration-100 active:scale-90 active:text-primary"
        >
          <SkipBack size={18} />
        </button>
        <button
          onclick={async (e) => {
            e.stopPropagation();
            await player.togglePlay();
          }}
          disabled={!player.currentSong}
          class="p-2 hover:scale-105 transition disabled:opacity-50 border border-white p-2"
        >
          {#if player.isPlaying}
            <Pause size={18} />
          {:else}
            <Play class="" size={18} />
          {/if}
        </button>
        <button
          onclick={handleNextSong}
          class="transition-all duration-100 active:scale-90 active:text-primary"
        >
          <SkipForward size={18} />
        </button>
      </div>
    </div>
  </div>
{:else}
  <div
    class="min-h-full flex flex-col w-full bg-background fixed top-0 right-0"
    transition:fly={{ y: 200, duration: 400, easing: cubicOut }}
  >
    <div
      class="bg-card flex py-1 border-b-3 border-border flex-row justify-between px-5 items-center"
    >
      <Button onclick={handleOpenAndClosePlayer} variant="ghost" class="p-2"
        ><ArrowLeft class="size-6" /></Button
      >
      <div class="flex flex-row w-auto items-center justify-center *:m-0 *:p-2">
        <Button variant="ghost">
          <Favorite class="size-6" />
        </Button>
        <Button variant="ghost"><EllipsisVertical class="size-6" /></Button>
      </div>
    </div>
    <div class="aspect-square h-auto p-5">
      <figure class="shadow-lg overflow-hidden">
        <img
          src={player.currentSong?.image
            ? Capacitor.convertFileSrc(player.currentSong.image)
            : defaulcover}
          loading="lazy"
          alt={player.currentSong?.title}
          class="w-full h-80 border-3 border-border object-cover"
        />
        <figcaption class="mt-5 flex flex-col gap-3">
          <h3 class=" text-xl font-extrabold">
            <MarqueeText
              text={player.currentSong?.title}
              class="pr-14 text-white font-black"
            />
          </h3>
          <p class="text-muted-foreground text-xs truncate">
            {player.currentSong?.artists}
          </p>
          <p
            class="text-muted-foreground text-xs truncate animate-seamless-marquee"
          >
            <MarqueeText
              text={player.currentSong?.album}
              class="pr-14 text-white font-medium"
            />
          </p>
        </figcaption>
      </figure>
    </div>
    <div
      class="px-5 flex flex-col items-center gap-2 w-full text-xs text-zinc-400"
    >
      <input
        type="range"
        min="0"
        max={player.duration || 100}
        value={displayTime}
        onpointerdown={handleSeekStart}
        oninput={handleSeekInput}
        onchange={handleSeekChange}
        disabled={!player.currentSong}
        style="background: linear-gradient(to right, oklch(0.424 0.199 265.638) {progressPercent}%, #3f3f46 {progressPercent}%);"
        class="w-full h-1 cursor-pointer appearance-none
    [&::-webkit-slider-thumb]:appearance-none
    [&::-webkit-slider-thumb]:w-1
    [&::-webkit-slider-thumb]:h-4
    [&::-webkit-slider-thumb]:bg-primary
 
    [&::-moz-range-thumb]:w-1
    [&::-moz-range-thumb]:h-4
    [&::-moz-range-thumb]:bg-white
    [&::-moz-range-thumb]:border-none
    [&::-moz-range-thumb]:rounded-full"
      />
      <div class="flex flex-row justify-between items-center w-full">
        <span class="text-sm text-muted-foreground"
          >{formatTime(displayTime)}</span
        >
        <span class="text-sm text-muted-foreground"
          >{formatTime(player.duration)}</span
        >
      </div>
    </div>
    <div
      class="flex flex-row justify-between w-full items-center px-5 flex-1"
      id="controles"
    >
      <button>
        <!--PONER EN BLANCO CUANDO ESTE ACTIVADOJK-->
        <Shuffle size={20} />
      </button>
      <button
        class="text-white transition-all duration-150 active:scale-90 active:text-primary"
        onclick={handlePreviousSong}
      >
        <SkipBack size={20} />
      </button>
      <Button
        class="bg-white aspect-square h-28 w-28"
        onclick={async (e) => {
          await player.togglePlay();
        }}
      >
        {#if !player.isPlaying}
          <PlayFilledAlt size={32} class="text-card fill-current size-10" />
        {:else}
          <PauseFilled size={32} class="text-card fill-current size-10" />
        {/if}
      </Button>
      <button
        class="text-white transition-all duration-150 active:scale-90 active:text-primary"
        onclick={handleNextSong}
      >
        <SkipForward size={20} />
      </button><button>
        <Repeat size={20} />
      </button>
    </div>
  </div>
{/if}
