<script>
  let { children } = $props();
</script>

<div
  class="flex overflow-x-auto space-x-4 px-1 py-2 snap-x snap-mandatory scrollbar-none [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
>
  {@render children()}
</div>
