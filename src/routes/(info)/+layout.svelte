<script lang="ts">
  import { ArrowLeft } from "@lucide/svelte";
  import Button from "$lib/components/ui/button/button.svelte";

  let { children } = $props();

  // Volver con el historial del WebView (consistente con el handler de back
  // de Android en el root layout). Si no hay historial, el <a href="/"> actúa
  // como fallback y navega al inicio.
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

    <h1 class="uppercase text-center text-2xl">Instrucciones</h1>

    <!-- Espaciador para mantener el título centrado, igual que el Hero -->
    <div class="w-18 shrink-0" aria-hidden="true"></div>
  </div>
</header>

<div class="flex-1 flex flex-col min-h-0 min-w-0 w-full overflow-hidden">
  {@render children()}
</div>
