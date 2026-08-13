<script lang="ts">
import { fade } from "svelte/transition";
  let { 
    title="", 
    img, 
    subtitle="", 
    width = "w-36", 
    height = "h-36",
    imgClass = "object-cover",
    onErrorImg = "/default-cover.png"
  } = $props();
  let loaded = $state(false);

  // Reinicia la carga si cambia la prop de la imagen
  $effect(() => {
    if (img) {
      loaded = false;
    }
  });
</script>

<div class="flex-shrink-0 {width} snap-start">
  <div
    class="{height} bg-zinc-800 overflow-hidden flex items-center justify-center relative"
  >
  {#if !loaded}
    <img 
      src={onErrorImg} 
      alt={title} 
      class="fallback absolute inset-0 w-full h-full object-cover"
      out:fade={{ duration: 300 }} 
    />
  {/if}
    <img
      src={img}
      class="w-full h-full {imgClass} hover:scale-105 transition-all duration-300 {loaded ? 'opacity-100' : 'opacity-0'}"
      alt="{title || 'Imagen'} "
      loading="lazy"
      onerror={(e) => {
        const target = e.target as HTMLImageElement;
        if (target.src !== window.location.origin + onErrorImg) {
          target.src = onErrorImg;
        }
        loaded = true;
      }}
      onload={() => loaded = true}
    />
  </div>

  {#if title}
    <p class="text-white text-sm mt-2 capitalize font-black truncate">
      {title}
    </p>
  {/if}

  {#if subtitle}
    <p class="text-muted-foreground text-sm capitalize font-medium truncate">
      {subtitle}
    </p>
  {/if}
</div>

<style>
  .fallback {
    z-index: 2;
    background-color: #2a2a2a;
  }
</style>
