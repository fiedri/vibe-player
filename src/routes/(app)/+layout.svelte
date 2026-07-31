<script lang="ts">
import Hero from "$lib/components/ui/hero/hero.svelte";
let {children} = $props()
</script>

  <Hero />

  <div class="flex-1 flex flex-col min-h-0 min-w-0 w-full overflow-hidden">
    {@render children()}
  </div>

