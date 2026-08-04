<script lang="ts">
  import { Pause, Play, ArrowLeft } from "@lucide/svelte";
  import { fly } from "svelte/transition";
  import { cubicOut } from "svelte/easing";
  import {
    PlayFilledAlt,
    SkipBack,
    SkipForward,
    Shuffle,
    Repeat,
    OverflowMenuVertical as EllipsisVertical,
    RepeatOne,
    PauseFilled,
    Favorite,
    FavoriteFilled,
  } from "carbon-icons-svelte";
import { player } from "./playerStore.svelte";
import { Capacitor } from "@capacitor/core";
import Button from "../button/button.svelte";
import MarqueeText from "../wrapper/marqueeText.svelte";
import { DialogType, ui } from "$lib/stores/ui.svelte";
import { formatearMS } from "$lib/utils";
  let audioElement = $state<HTMLAudioElement | null>(null);
  let isSeeking = $state<boolean>(false);
  let seekValue = $state<number>(0);

  $effect(() => {
    player.playTrigger;
    const song = player.currentSong;
    const isPlaying = player.isPlaying;
    if (!audioElement || !song) return;

    if (isPlaying) {
      const playPromise = audioElement.play();
      if (playPromise !== undefined) {
        playPromise.catch((error) => {
          if (error.name !== "AbortError") {
            console.error("Error al reproducir audio:", error);
            player.pause();
          }
        });
      }
    }
  });

  $effect(() => {
    if (audioElement) {
      player.onSeekRequest = (time: number) => {
        if (audioElement) {
          audioElement.currentTime = time;
        }
      };
      player.onPauseRequest = () => audioElement?.pause();
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
    // El nuevo src ya cargó: la transición terminó, se levanta la supresión.
    player.endNativePauseSuppression();
    player.duration = audioElement.duration;
    if (player.currentTime > 0 && player.currentTime < audioElement.duration) {
      audioElement.currentTime = player.currentTime;
    }
    player.updatePositionState(player.currentTime, player.duration, true);
  }

  function handleSeekChange(e: Event) {
    const target = e.target as HTMLInputElement;
    const newTime = parseFloat(target.value);
    if (audioElement) {
      audioElement.currentTime = newTime;
    }
    player.currentTime = newTime;
    isSeeking = false;
  }

  function handlePlay() {
    player.isPlaying = true;
    player.endNativePauseSuppression();
    player.syncNativePlaybackState(true);
  }

  function handlePause() {
    if (player.isSuppressingNativePause) {
      return;
    }
    player.isPlaying = false;
    player.syncNativePlaybackState(false);
  }

  function handleEnded() {
    player.next();
  }

  function handleError(e: Event) {
    console.error("Error en elemento audio:", e);
    player.isPlaying = false;
    player.endNativePauseSuppression();
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
  function handleShuffle() {
    player.toggleShuffle();
    if (player.isShuffle) {
      showNotificacion("Modo Aleatorio (activo)");
    } else {
      showNotificacion("Modo Aleatorio (desactivado)");
    }
  }
  const REPEAT_TRANSITIONS = {
    off: { next: "one", msg: "Repetir una" },
    one: { next: "all", msg: "Repetir todas" },
    all: { next: "off", msg: "Repetición desactivada" },
  };

  function handleChangeRepeatMode() {
    const { next, msg } = REPEAT_TRANSITIONS[player.mode];
    player.mode = next;
    showNotificacion(msg);
  }

  let needNotify = $state(false);
  let notification = $state("");
  function showNotificacion(Message) {
    notification = Message;
    needNotify = true;
    setTimeout(() => {
      needNotify = false;
    }, 1000);
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
    class="bg-card w-full sticky z-100 overflow-hidden md:hidden"
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
            src={Capacitor.convertFileSrc(player.currentSong.image)}
            loading="lazy"
            alt={player.currentSong.title}
            class="w-14 h-14 border border-border object-cover"
            onerror={(e) => {
              const target = e.target as HTMLImageElement;
              if (
                target.src !==
                window.location.origin + "/default-cover.png"
              ) {
                target.src = "/default-cover.png";
              }
            }}
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
          onclick={(e) => {
            e.stopPropagation();
            player.togglePlay();
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
  {#if needNotify}
    <div
      class="fixed bg-card/70 text-muted-foreground z-30 text-base rounded px-5 bottom-8 left-[50%] translate-x-[-50%] animate_slideUp text-center"
    >
      {notification}
    </div>
  {/if}
  <div
    class="min-h-full flex flex-col w-full bg-background fixed top-0 right-0 z-10"
    transition:fly={{ y: 200, duration: 400, easing: cubicOut }}
  >
    <div
      class="bg-background flex py-1 border-b-3 border-border flex-row justify-between px-5 items-center"
    >
      <Button onclick={handleOpenAndClosePlayer} variant="ghost" class="p-2"
        ><ArrowLeft class="size-6" /></Button
      >
      <div class="flex flex-row w-auto items-center justify-center *:m-0 *:p-2">
        <Button variant="ghost" onclick={() => ui.openDialog(DialogType.Unimplemented)}>
          <Favorite class="size-6" />
        </Button>
        <Button variant="ghost" onclick={() => ui.openDialog(DialogType.Unimplemented)}><EllipsisVertical class="size-6" /></Button>
      </div>
    </div>
    <div class="aspect-square h-auto p-5">
      <figure class="shadow-lg overflow-hidden">
        <img
          src={Capacitor.convertFileSrc(player.currentSong.image)}
          loading="lazy"
          alt={player.currentSong?.title}
          class="w-full h-80 border-3 border-border object-cover"
          onerror={(e) => {
            const target = e.target as HTMLImageElement;
            if (target.src !== window.location.origin + "/default-cover.png") {
              target.src = "/default-cover.png";
            }
          }}
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
          >{formatearMS(displayTime * 1000)}</span
        >
        <span class="text-sm text-muted-foreground"
          >{formatearMS(player.duration * 1000)}</span
        >
      </div>
    </div>
    <div
      class="flex flex-row justify-between w-full items-center px-5 flex-1"
      id="controles"
    >
      <button
        onclick={handleShuffle}
        class={player.isShuffle ? "" : "text-muted-foreground"}
      >
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
        onclick={(e) => {
          player.togglePlay();
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
      </button>
      <button
        onclick={handleChangeRepeatMode}
        class={player.mode !== 'off'
          ? ""
          : "text-muted-foreground"}
      >
        {#if player.mode == 'one'}
          <RepeatOne size={20} />
        {:else}
          <Repeat size={20} />
        {/if}
      </button>
    </div>
  </div>
{/if}
