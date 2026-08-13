<script lang="ts">
  import Button from "../button/button.svelte";
  import { page } from "$app/stores";
  import {
    OverflowMenuVertical,
    Information as Info,
    Menu,
    Search,
  } from "carbon-icons-svelte";
  import { crossfade } from "svelte/transition";
  import { cubicInOut } from "svelte/easing";
  import { ui } from "$lib/stores/ui.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  let tabs = [
    { href: "/", tab: "Inicios" },
    { href: "/songs", tab: "Canciones" },
    { href: "/albums", tab: "Álbumes" },
    { href: "/artist", tab: "Artistas" },
    { href: "/playlist", tab: "Playlist" },
  ];
  let overflowMenu = [
    {
      title: "Actualizar Biblioteca",
      action: () => {
        biblioteca.refresh();
      },
    },
  ];

  let activeTab = $derived($page.url.pathname);

  // Animación crossfade
  const [send, receive] = crossfade({
    duration: 300,
    easing: cubicInOut,
  });

  // Menú de hamburguesa
  let menuOpen = $state(false);
  let overflowMenuOpen = $state(false);
  function closeMenu() {
    menuOpen = false;
  }

  // Cierra el menú al tocar fuera del wrapper (botón + panel)
  function clickOutside(node: Element) {
    function onPointerDown(e: PointerEvent) {
      if (menuOpen && !node.contains(e.target as Node)) closeMenu();
    }
    document.addEventListener("pointerdown", onPointerDown, true);
    return () => {
      document.removeEventListener("pointerdown", onPointerDown, true);
    };
  }
</script>

<!-- Cerrar con Escape (útil en desktop/dev) -->
<svelte:window
  onkeydown={(e) => {
    if (e.key === "Escape") closeMenu();
  }}
/>

<section id="hero" class="pt-2 px-2 border-b-4 border-border">
  <div class="flex flex-row justify-between items-center w-full mb-5">
    <div class="relative" {@attach clickOutside}>
      <Button
        variant="ghost"
        class="px-2"
        aria-haspopup="menu"
        aria-expanded={menuOpen}
        aria-label="Abrir menú"
        onclick={() => (menuOpen = !menuOpen)}
      >
        <Menu class="size-6" />
      </Button>

      {#if menuOpen}
        <div
          class="absolute top-full left-0 mt-2 z-50 min-w-44 bg-card border border-border shadow-lg"
          role="menu"
        >
          <a
            href="/info"
            role="menuitem"
            onclick={closeMenu}
            class="flex items-center gap-2 p-3 text-sm text-muted-foreground hover:text-white hover:bg-zinc-800/50"
          >
            <Info class="size-4 shrink-0" />
            <span>Instrucciones</span>
          </a>
        </div>
      {/if}
    </div>

    <h1 class="uppercase text-center text-2xl">Vibe</h1>
    <div class="relative">
      <Button variant="ghost"
        class="px-1"
        href="/search"
      ><Search class="size-6" /></Button>
      <Button
        variant="ghost"
        class="px-0"
        onclick={() => {
          overflowMenuOpen = !overflowMenuOpen;
        }}
      >
        <OverflowMenuVertical class="size-6" />
      </Button>
      {#if overflowMenuOpen}
        <button
          class="fixed h-screen w-screen inset-0 z-[100] flex items-center justify-center"
          onclick={() => {
            overflowMenuOpen = false;
          }}
          aria-label="Cerrar menú"
        >
        </button>
        <div
          class="absolute top-full w-50 right-4 h-auto bg-popover border border-border z-[9999]"
        >
          {#each overflowMenu as options}
            <button
              class="p-2"
              onclick={() => {
                options.action();
                overflowMenuOpen = false;
              }}
            >
              {options.title}
            </button>
          {/each}
        </div>
      {/if}
    </div>
  </div>
  <!--<input
      class="appearance-none border-2 bg-input hover:border-border/70 transition-colors w-full py-3 px-3 leading-tight focus:outline-none focus:ring-ring focus:border-border focus:shadow-outline border-border"
      id="username"
      type="text"
      placeholder="Search..."
      bind:value={ui.query}
    />-->

  <nav>
    <ul class="flex flex-row relative overflow-x-auto">
      {#each tabs as tab (tab.href)}
        {@const isActive = activeTab === tab.href}
        <li class="relative">
          <a
            href={tab.href}
            class="flex h-10 items-center justify-center p-1 px-2 text-sm transition-colors duration-200 {isActive
              ? 'text-white font-medium'
              : 'text-muted-foreground hover:text-zinc-300'}"
          >
            {tab.tab}
          </a>

          {#if isActive}
            <div
              class="absolute bottom-0 left-0 right-0 h-[2px] bg-primary"
              in:send={{ key: "active-tab" }}
              out:receive={{ key: "active-tab" }}
            ></div>
          {/if}
        </li>
      {/each}
    </ul>
  </nav>
</section>
