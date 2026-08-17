<script lang="ts">
  import { fade } from "svelte/transition";

  let {
    title = "",
    img,
    subtitle = "",
    width = "w-36",
    height = "h-36",
    imgClass = "object-cover",
    onErrorImg = "/default-cover.png"
  } = $props();

  let loaded = $state(false);
  let failed = $state(false);
  let currentImg = $state("");

  // Reinicia la carga si cambia la prop de la imagen
  $effect(() => {
    if (img) {
      currentImg = img;
      loaded = false;
      failed = false;
    }
  });
</script>

<div class="flex-shrink-0 {width} snap-start">
  <div
    class="{height} bg-zinc-800 overflow-hidden flex items-center justify-center relative"
  >
    {#if !loaded && !failed}
      <div
        class="absolute inset-0 animate-pulse bg-zinc-700 rounded"
        out:fade={{ duration: 300 }}
      ></div>
    {/if}
    <img
      src={currentImg}
      class="w-full h-full {imgClass} hover:scale-105 transition-all duration-300 {loaded ? 'opacity-100' : 'opacity-0'}"
      alt="{title || 'Imagen'} "
      loading="lazy"
      decoding="async"
      onerror={() => {
        if (!failed && onErrorImg) {
          failed = true;
          currentImg = onErrorImg;
        }
      }}
      onload={() => {
        loaded = true;
        failed = false;
      }}
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
