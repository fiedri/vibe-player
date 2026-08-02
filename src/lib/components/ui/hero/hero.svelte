<script lang="ts">
  import Button from "../button/button.svelte";
  import { page } from "$app/stores";
  import {
    OverflowMenuVertical,
    Renew,
    Information as Info,
    Menu,
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
      icon: Renew,
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

<section id="hero" class="pt-5 px-2 border-b-4 border-border">
  <div class="flex flex-row justify-between items-center w-full mb-5">
    <div class="relative" {@attach clickOutside}>
      <Button
        variant="ghost"
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
    <div>
      <Button
        variant="ghost"
        onclick={() => {
          overflowMenuOpen = !overflowMenuOpen;
        }}
      >
        <OverflowMenuVertical class="size-5" />
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
          class="absolute w-50 right-4 h-auto bg-popover border border-border z-[9999]"
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
  <div class="relative w-[95%] mx-auto mb-4">
    <input
      class="appearance-none border-2 bg-input hover:border-border/70 transition-colors w-full py-3 px-3 leading-tight focus:outline-none focus:ring-ring focus:border-border focus:shadow-outline border-border"
      id="username"
      type="text"
      placeholder="Search..."
      bind:value={ui.query}
    />

    <div class="absolute right-0 inset-y-0 flex mr-4 items-center">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-6 w-6 ml-3 hover:text-primary/70"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
        />
      </svg>
    </div>
  </div>

  <nav>
    <ul class="flex flex-row relative">
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
