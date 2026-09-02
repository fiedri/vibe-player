<script lang="ts">
  import { ArrowLeft } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import { m } from "$lib/paraglide/messages.js";

  let { children } = $props();

  function goBack(e: MouseEvent) {
    if (window.history.length > 1) {
      e.preventDefault();
      window.history.back();
    }
  }
</script>

<header class="pt-5 px-2 border-b-4 border-border">
  <div class="flex flex-row justify-between items-center w-full mb-5">
    <Button variant="ghost" href="/" aria-label="Volver" onclick={goBack}>
      <ArrowLeft class="size-6" />
    </Button>

    <h1 class="uppercase text-center text-2xl">{m["info_page.title"]()}</h1>

    <div class="w-18 shrink-0" aria-hidden="true"></div>
  </div>
</header>

<div class="flex-1 flex flex-col min-h-0 min-w-0 w-full overflow-hidden">
  {@render children()}
</div>
