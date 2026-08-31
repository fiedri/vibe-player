<script lang="ts">
  import { ArrowLeft, Home, Restart } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import { goto } from "$app/navigation";

  let { status, error }: { status: number; error: App.Error } = $props();

  let es404 = $derived(status === 404);
  let titulo = $derived(
    es404 ? "No encontramos esta vista" : "Se cortó la música",
  );
  const DEFAULTS = ["Internal Error", "Not Found", ""];
  let detalle = $derived(
    es404
      ? "La página que buscás no existe o se movió de lugar."
      : DEFAULTS.includes(error?.message ?? "")
        ? "Algo se rompió mientras cargábamos esta vista. Probá de nuevo."
        : (error?.message ?? "Algo salió mal."),
  );

  function volver() {
    if (window.history.length > 1) {
      window.history.back();
    } else {
      goto("/");
    }
  }

  function reintentar() {
    location.reload();
  }

  let tight = $state(false);
  let scrollTop = $state(0);
  function handleScroll(e: Event) {
    const target = e.currentTarget as HTMLElement;
    scrollTop = target.scrollTop;
    tight = scrollTop > 60;
  }
</script>

<section
  class="h-screen w-screen overflow-y-auto overscroll-y-contain"
  id="error-view"
  onscroll={handleScroll}
>
  <div class="top-0 z-10 py-1 absolute">
    <Button
      variant="ghost"
      class="px-4 transition-all {tight ? 'bg-background' : 'bg-transparent'}"
      onclick={volver}
      aria-label="Volver atrás"
    >
      <ArrowLeft class="size-6" />
    </Button>
  </div>

  <div
    class="error-bg min-h-full flex flex-col items-center justify-center gap-4 px-6 text-center"
  >
    <div
      class="vinyl relative size-40 rounded-full shadow-2xl"
      aria-hidden="true"
    >
      <div
        class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 size-20 rounded-full bg-primary text-primary-foreground flex items-center justify-center"
      >
        <span class="font-black text-2xl tracking-tight">{status}</span>
      </div>
      <svg
        class="absolute inset-0 size-full"
        viewBox="0 0 100 100"
        fill="none"
      >
        <path
          d="M14 62 L 86 42"
          stroke="white"
          stroke-opacity="0.35"
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          d="M18 66 L 82 50"
          stroke="white"
          stroke-opacity="0.25"
          stroke-width="1.5"
          stroke-linecap="round"
        />
        <path
          d="M76 38 L 87 33"
          stroke="white"
          stroke-opacity="0.3"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path
          d="M20 70 L 30 60"
          stroke="white"
          stroke-opacity="0.3"
          stroke-width="2"
          stroke-linecap="round"
        />
      </svg>
    </div>

    <h1 class="text-3xl font-extrabold">{titulo}</h1>
    <p class="text-sm text-muted-foreground max-w-xs">{detalle}</p>

    <div class="mt-2 flex w-full max-w-xs flex-col gap-3">
      <Button
        class="h-12 w-full bg-white text-background font-semibold active:scale-95 transition-transform"
        onclick={volver}
      >
        <Home class="fill-current" />
        Volver al inicio
      </Button>
      <Button
        variant="outline"
        class="h-12 w-full border-2 border-border font-semibold active:scale-95 transition-transform"
        onclick={reintentar}
      >
        <Restart />
        Reintentar
      </Button>
    </div>
  </div>
</section>

<style>
  .error-bg {
    background-image:
      radial-gradient(
        ellipse 80% 55% at 50% 0%,
        oklch(0.424 0.199 265.638 / 0.14),
        transparent 70%
      ),
      radial-gradient(
        ellipse 60% 40% at 50% 100%,
        oklch(0.424 0.199 265.638 / 0.06),
        transparent 70%
      );
  }

  .vinyl {
    background:
      radial-gradient(
        circle at 50% 50%,
        #0f0f0f 0 2px,
        transparent 2.5px
      ),
      repeating-radial-gradient(
        circle at 50% 50%,
        #0d0d0d 0 1px,
        #1b1b1b 1px 2px
      );
    box-shadow:
      inset 0 0 0 1px #232323,
      0 24px 60px -12px rgba(0, 0, 0, 0.7);
  }

  .vinyl::after {
    content: "";
    position: absolute;
    inset: 0;
    border-radius: 9999px;
    background: conic-gradient(
      from 205deg,
      transparent 0deg,
      rgba(255, 255, 255, 0.045) 38deg,
      transparent 75deg
    );
  }
</style>