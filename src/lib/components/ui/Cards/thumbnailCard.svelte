<script>
  let { 
    title="", 
    img, 
    subtitle="", 
    width = "w-36", 
    height = "h-36",
    imgClass = "object-cover"
  } = $props();
</script>

<div class="flex-shrink-0 {width} snap-start">
  <div class="{height} bg-zinc-800 overflow-hidden flex items-center justify-center">
    <img 
      src={img} 
      class="w-full h-full {imgClass} hover:scale-105 transition-all duration-300" 
      alt="{title || 'Imagen'} " 
    />
  </div>

  {#if title}
    <p class="text-white text-sm mt-2 capitalize font-black truncate">{title}</p>
  {/if}

  {#if subtitle}
    <p class="text-muted-foreground text-sm capitalize font-medium truncate">{subtitle}</p>
  {/if}
</div>
