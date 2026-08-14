<script lang="ts">
  import { fly } from "svelte/transition";
  import { cubicOut } from "svelte/easing";
  import {
    PlayFilledAlt,
    SkipBack,
    ArrowLeft,
    SkipForward,
    Shuffle,
    Repeat,
    OverflowMenuVertical as EllipsisVertical,
    RepeatOne,
    PauseFilled,
    Favorite,
    FavoriteFilled,
  } from "carbon-icons-svelte";
  import { Capacitor } from "@capacitor/core";
  import { playerService } from "$lib/services/player/PlayerFacade";
  import {
    displayTitle,
    displayArtist,
    displayAlbum,
    displayImage,
    DEFAULT_COVER,
  } from "$lib/types/songs";
  import Button from "../button/button.svelte";
  import MarqueeText from "../wrapper/marqueeText.svelte";
  import { DialogType, ui } from "$lib/stores/ui.svelte";
  import { formatearMS } from "$lib/utils";
  import { favorites } from "$lib/stores/favorites.svelte";
  import PlayerMenu from "../menus/playerMenu.svelte";
  let audioElement = $state<HTMLAudioElement | null>(null);
  let isSeeking = $state<boolean>(false);
  let seekValue = $state<number>(0);

  $effect(() => {
    if (audioElement) {
      playerService.attachElement(audioElement);
    }
  });


  let previousPlayTrigger = 0;
  $effect(() => {
    const trigger = playerService.playTrigger;
    if (!audioElement) return;
    const song = playerService.currentSong;
    if (trigger <= previousPlayTrigger || !song) return;
    previousPlayTrigger = trigger;

    const playPromise = audioElement.play();
    if (playPromise !== undefined) {
      playPromise.catch((error) => {
        if (error.name !== "AbortError") {
          console.error("Error al reproducir audio:", error);
          playerService.pause();
        }
      });
    }
  });

  let displayTime = $derived(isSeeking ? seekValue : playerService.currentTime);
  let progressPercent = $derived(
    playerService.duration ? (displayTime / playerService.duration) * 100 : 0,
  );

  function handleSeekChange(e: Event) {
    const target = e.target as HTMLInputElement;
    const newTime = parseFloat(target.value);

    playerService.seekTo(newTime);
    isSeeking = false;
  }

  function handlePlay() {
    playerService.isPlaying = true;
    playerService.endNativePauseSuppression();
    playerService.syncNativePlaybackState(true);
  }

  function handlePause() {
    if (playerService.isSuppressingNativePause) {
      return;
    }
    playerService.isPlaying = false;
    playerService.syncNativePlaybackState(false);
  }

  function handleEnded() {
    playerService.handleTrackEnded();
  }

  function handleError(e: Event) {
    console.error("Error en elemento audio:", e);
    playerService.pause();
    playerService.endNativePauseSuppression();
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
    if (ui.playerIsOpen) {
      ui.playerIsOpen = false;
      return;
    }
    if (!playerService.currentSong) return;
    ui.playerIsOpen = true;
  }

  function handleNextSong(e?: Event) {
    if (e) e.stopPropagation();
    playerService.next();
  }

  function handlePreviousSong(e?: MouseEvent | Event) {
    if (e) e.stopPropagation();
    playerService.previous();
  }
  function handleShuffle() {
    playerService.toggleShuffle();
    if (playerService.isShuffle) {
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
let repeatMode = $state(playerService.mode);
  function handleChangeRepeatMode() {
    const { next, msg } =
     REPEAT_TRANSITIONS[playerService.mode as keyof typeof REPEAT_TRANSITIONS];
   playerService.switchMode(next);
   repeatMode = next
    showNotificacion(msg);
  }

let isOpenMenu = $state(false)
  let needNotify = $state(false);
  let notification = $state("");
  function showNotificacion(Message: string) {
    notification = Message;
    needNotify = true;
    setTimeout(() => {
      needNotify = false;
    }, 1000);
  }
</script>

{#if playerService.currentSong}
  <audio
    bind:this={audioElement}
    src={playerService.currentSong?.uri
      ? Capacitor.convertFileSrc(playerService.currentSong.uri)
      : ""}
    bind:volume={playerService.volume}
    onloadedmetadata={() => playerService.handleLoadedMetadata()}
    ontimeupdate={() => playerService.syncPlaybackPosition()}
    onplay={handlePlay}
    onpause={handlePause}
    onended={handleEnded}
    onerror={handleError}
  ></audio>
{/if}
{#if !ui.playerIsOpen}
  <!-- svelte-ignore a11y_click_events_have_key_events -->
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <div
    class="bg-card w-full sticky z-[9999] overflow-hidden md:hidden"
    onclick={handleOpenAndClosePlayer}
  >
    <div class="flex items-center gap-2 w-full text-xs text-zinc-400">
      <input
        type="range"
        min="0"
        max={playerService.duration || 100}
        value={displayTime}
        onpointerdown={handleSeekStart}
        oninput={handleSeekInput}
        onchange={handleSeekChange}
        disabled={!playerService.currentSong}
        style="background: linear-gradient(to right,oklch(0.424 0.199 265.638) {progressPercent}%, #3f3f46 {progressPercent}%);"
        class="w-full h-1 cursor-pointer appearance-none [&::-webkit-slider-thumb]:appearance-none [&::-moz-range-thumb]:appearance-none"
      />
    </div>
    <div class="w-full flex justify-between items-center h-20 px-3">
      <div class="flex items-center gap-3 w-[50%]">
        {#if playerService.currentSong}
          <img
            src={Capacitor.convertFileSrc(displayImage(playerService.currentSong))}
            loading="lazy"
            alt={displayTitle(playerService.currentSong)}
            class="w-14 h-14 border border-border object-cover"
            onerror={(e) => {
              const target = e.target as HTMLImageElement;
              if (
                target.src !==
                window.location.origin + DEFAULT_COVER
              ) {
                target.src = DEFAULT_COVER;
              }
            }}
          />
          <div class="flex flex-col overflow-hidden">
            <MarqueeText
              text={displayTitle(playerService.currentSong)}
              class="pr-14 text-white font-black"
            />
            <span class="text-xs text-muted-foreground truncate"
              >{displayArtist(playerService.currentSong)}</span
            >
          </div>
        {:else}
          <div class="text-xs text-zinc-500">Sin Canción</div>
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
            playerService.togglePlay();
          }}
          disabled={!playerService.currentSong}
          class="p-2 hover:scale-105 transition disabled:opacity-50 border border-white p-2"
        >
          {#if playerService.isPlaying}
            <PauseFilled size={18} />
          {:else}
            <PlayFilledAlt class="" size={18} />
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
      class="fixed bg-card/70 text-muted-foreground z-30 text-base px-5 py-2 bottom-8 left-[50%] translate-x-[-50%] animate_slideUp text-center"
    >
      {notification}
    </div>
  {/if}
  <div
    class="min-h-full flex flex-col w-full bg-background fixed top-0 right-0 z-10"
    style="background-image: radial-gradient(ellipse 90% 45% at 50% -5%, color-mix(in oklab, var(--primary) 10%, transparent), transparent 70%);"
    transition:fly={{ y: 200, duration: 400, easing: cubicOut }}
  >
    <div
      class="flex py-1 absolute w-full z-[100] flex-row justify-between px-5 items-center bg-card/10"
    >
      <Button
        onclick={handleOpenAndClosePlayer}
        variant="ghost"
        class="p-2 active:scale-90 transition-transform"
        ><ArrowLeft class="size-6" /></Button
      >
      <div class="flex flex-row w-auto items-center justify-center">
        <Button
          variant="ghost"
          class="active:scale-90 transition-transform m-0 p-2"
          onclick={() => playerService.currentSong?.id && favorites.toggleFavorite(playerService.currentSong.id)}
        >
        {#if playerService.currentSong?.id && favorites.songsIds.has(playerService.currentSong.id)}
          
        <FavoriteFilled class="size-6"/>
        {:else}

          <Favorite class="size-6" />
        {/if}
        </Button>
        <Button
          variant="ghost"
          class="active:scale-90 transition-transform m-0 p-2"
          onclick={() => isOpenMenu = !isOpenMenu}
          ><EllipsisVertical class="size-6" /></Button
        >
       {#if isOpenMenu}
        
<PlayerMenu onClose={()=> isOpenMenu= false} song={playerService.currentSong}/>
       {/if }
      </div>
    </div>
    <div >
      <figure>
        <img
          src={Capacitor.convertFileSrc(displayImage(playerService.currentSong))}
          loading="lazy"
          alt={displayTitle(playerService.currentSong)}
          class="w-full aspect-square border-2 border-border object-cover shadow-2xl"
          style="filter: brightness(0.82) saturate(0.9); box-shadow: 0 0 80px 12px color-mix(in oklab, var(--primary) 50%, transparent);"
          onerror={(e) => {
            const target = e.target as HTMLImageElement;
            if (target.src !== window.location.origin + DEFAULT_COVER) {
              target.src = DEFAULT_COVER;
            }
          }}
        />
        <figcaption class="mt-5 px-5 mb-5 flex flex-col gap-3">
          <h3 class=" text-xl font-extrabold">
            <MarqueeText
              text={displayTitle(playerService.currentSong)}
              class="pr-14 text-white font-black"
            />
          </h3>
          <p class="text-muted-foreground text-xs truncate">
            {displayArtist(playerService.currentSong)}
          </p>
          <p
            class="text-muted-foreground text-xs truncate animate-seamless-marquee"
          >
            <MarqueeText
              text={displayAlbum(playerService.currentSong)}
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
        max={playerService.duration || 100}
        value={displayTime}
        onpointerdown={handleSeekStart}
        oninput={handleSeekInput}
        onchange={handleSeekChange}
        disabled={!playerService.currentSong}
        style="background: linear-gradient(to right, var(--primary) {progressPercent}%, var(--border) {progressPercent}%);"
        class="w-full h-1 cursor-pointer appearance-none
    [&::-webkit-slider-thumb]:appearance-none
    [&::-webkit-slider-thumb]:w-1.5
    [&::-webkit-slider-thumb]:h-4
    [&::-webkit-slider-thumb]:bg-primary

    [&::-moz-range-thumb]:w-1.5
    [&::-moz-range-thumb]:h-4
    [&::-moz-range-thumb]:bg-white
    [&::-moz-range-thumb]:border-none"
      />
      <div class="flex flex-row justify-between items-center w-full">
        <span class="text-sm text-muted-foreground"
          >{formatearMS(displayTime * 1000)}</span
        >
        <span class="text-[10px] text-muted-foreground">
          {playerService.currentSongIndex + 1}/
          {playerService.numberOfSongs}
        </span>
        <span class="text-sm text-muted-foreground"
          >{playerService.duration
            ? formatearMS(playerService.duration * 1000)
            : "00:00"}</span
        >
      </div>
    </div>
    <div
      class="flex flex-row justify-between w-full items-center px-5 flex-1"
      id="controles"
    >
      <button
        onclick={handleShuffle}
        class={playerService.isShuffle ? "text-primary transition-all duration-150" : "text-muted-foreground transition-all duration-150"}
      >
        <Shuffle size={20} />
      </button>
      <button
        class="text-white transition-all duration-150 active:scale-90 active:text-primary"
        onclick={handlePreviousSong}
      >
        <SkipBack size={20} />
      </button>
      <Button
        class="bg-white aspect-square h-28 w-28 shadow-xl transition-transform active:scale-95"
        onclick={(e) => {
          playerService.togglePlay();
        }}
      >
        {#if !playerService.isPlaying}
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
        class={repeatMode !== "off" ? "text-primary transition-all duration-150" : "text-muted-foreground transition-all duration-150"}
      >
        {#if repeatMode == "one"}
          <RepeatOne size={20} />
        {:else}
          <Repeat size={20} />
        {/if}
      </button>    </div>
  </div>
{/if}
