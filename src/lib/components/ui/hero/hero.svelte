<script lang="ts">
import { m } from "$lib/paraglide/messages.js";

  import Button from "../button/button.svelte";
  import { page } from "$app/stores";
  import {
    OverflowMenuVertical,
    Information as Info,
    Menu,
    Search,
    ArrowLeft,
    Settings as SettingsIcon,
  } from "carbon-icons-svelte";
  import { crossfade, fly } from "svelte/transition";
  import { cubicInOut } from "svelte/easing";
  import MainMenu from "../menus/mainMenu.svelte";
    import { getSortableStoreByPath } from "$lib/services/storeRegistry";
  let tabs = [
    { href: "/", tab: m["tabs.songs"]() },
    { href: "/albums", tab: m["tabs.albums"]() },
    { href: "/artist", tab: m["tabs.artists"]() },
    { href: "/playlist", tab: "Playlist" },
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
  function onCloseOverflowMenu() {
    overflowMenuOpen = false;
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
  const store = $derived(getSortableStoreByPath(activeTab))
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
          class="fixed inset-y-0 left-0 z-50 w-[70%] flex flex-col bg-card border-r border-border shadow-lg"
          role="menu"
          transition:fly={{ x: -100, duration: 200 }}
        >
          <div class="flex items-center p-3 border-b border-border">
            <Button
              variant="ghost"
              size="icon-sm"
              aria-label="Cerrar menú"
              onclick={closeMenu}
            >
              <ArrowLeft class="size-6" />
            </Button>
          </div>

          <a
            href="/info"
            role="menuitem"
            onclick={closeMenu}
            class="flex items-center gap-2 p-3 text-sm text-muted-foreground hover:text-white hover:bg-zinc-800/50"
          >
            <Info class="size-4 shrink-0" />
            <span>{m["menus.mainmenu.instructions"]()}</span>
          </a>

          <a
            href="/settings"
            role="menuitem"
            onclick={closeMenu}
            class="flex items-center gap-2 p-3 text-sm text-muted-foreground hover:text-white hover:bg-zinc-800/50"
          >
            <SettingsIcon class="size-4 shrink-0" />
            <span>{m["menus.mainmenu.settings"]()}</span>
          </a>
        </div>
      {/if}
    </div>

    <h1 class="uppercase text-center text-2xl">Vibe</h1>
    <div class="relative">
      <Button variant="ghost" class="px-1" href="/search"
        ><Search class="size-6" /></Button
      >
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
        
      {/if}
      <MainMenu isOpen={overflowMenuOpen} onClose={onCloseOverflowMenu} activeStore={store}/>
    </div>
  </div>

  <nav>
    <ul class="flex flex-row relative overflow-x-auto justify-center">
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
