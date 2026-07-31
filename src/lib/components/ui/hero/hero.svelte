<script>
  import { Menu } from "@lucide/svelte";
  import Button from "../button/button.svelte";
  import { page } from "$app/stores";
  import { OverflowMenuVertical } from "carbon-icons-svelte";
  import { crossfade } from "svelte/transition";
  import { cubicInOut } from "svelte/easing";

  let tabs = [
    { href: "/", tab: "Inicios" },
    { href: "/songs", tab: "Canciones" },
    { href: "/albums", tab: "Álbumes" },
    { href: "/artist", tab: "Artistas" },
    { href: "/playlist", tab: "Playlist" },
  ];

  let activeTab = $derived($page.url.pathname);

  // Animación crossfade
  const [send, receive] = crossfade({
    duration: 300,
    easing: cubicInOut,
  });
</script>

<section id="hero" class="pt-5 px-2 border-b-4 border-border">
 <div class="flex flex-row justify-between items-center w-full mb-5">
<Button variant="ghost" >
    <Menu class="size-6" />
  </Button>

  <h1 class="uppercase text-center text-2xl">Vibe</h1>
<Button variant="ghost">
    <OverflowMenuVertical class="size-5" />
  </Button>
 </div> 
  <div class="relative w-[95%] mx-auto mb-4">
    <input
      class="appearance-none border-2 bg-input hover:border-border/70 transition-colors w-full py-3 px-3 leading-tight focus:outline-none focus:ring-ring focus:border-border focus:shadow-outline border-border"
      id="username"
      type="text"
      placeholder="Search..."
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
      {#each tabs as tab}
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
