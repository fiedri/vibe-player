<script lang="ts">
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import HorizontalContainer from "$lib/components/ui/wrapper/horizontalContainer.svelte";
  import { ui } from "$lib/stores/ui.svelte";
  // Generamos 2.000 canciones de prueba
  const mockSongs = Array.from({ length: 2000 }, (_, i) => ({
    id: `${i}`,
    title: `Canción Virtual #${i + 1}`,
    artists: `Artista #${(i % 10) + 1}`,
    album: "Álbum de Prueba",
    duration: 180,
    path: `/test/${i}.mp3`,
    audioUrl: "",
    image: "/default-cover.png",
  }));

  function greeting() {
    const currentTime = new Date();
    const currentHour = currentTime.getHours();
    let greeting = "";

    if (currentHour < 12) {
      greeting = "Buenos dias";
    } else if (currentHour < 18) {
      greeting = "Buenas tardes";
    } else {
      greeting = "Buenas Noches";
    }
    return greeting;
  }
  let mockPlaylist = Array.from({ length: 5 }, (_, i) => ({
    title: "Ejemplo",
    portada: "/default-cover.png",
  }));
  let mockArtist = Array.from({ length: 5 }, (_, i) => ({
    title: "Ejemplo",
    portada: "/default-cover.png",
  }));
</script>

<div
  class=" h-full w-full min-w-0 overflow-x-hidden flex p-5 pb-10 flex-col gap-15 min-h-0 overflow-y-auto [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
>
  <div class="w-full mb-6">
    <div class="flex flex-row justify-between items-center">
      <h2>Tus Playlists</h2>
      <a href="" class="mb-3 underline text-muted-foreground text-sm"
        onclick={(e) => { e.preventDefault(); ui.handleDialog(); }}
        >Ver todas</a
      >
    </div>

    <HorizontalContainer>
      {#each mockPlaylist as playlist}
        <!--Card-->
        <ThumbnailCard title={playlist.title} img={playlist.portada} />
      {/each}
    </HorizontalContainer>
  </div>

  <div class="w-full mb-6">
    <h2>Agregados Recientemente</h2>
    <!-- Tu contenido aquí -->
    <div class="flex flex-col gap-3">
      {#each mockSongs.slice(0, 3) as song, idx}
        <div class="h-12 flex gap-3">
          <img src={song.image} alt="" class="border-2 border-border" />
          <div class="min-w-0 flex-1">
            <p class="font-medium hover:underline text-white truncate">
              {song.title}
            </p>
            <p class="text-xs text-muted-foreground hover:underline truncate">
              {song.artists}
            </p>
          </div>
        </div>
      {/each}
    </div>
  </div>

  <div class="w-full mb-6">
    <h2>Las Mas Escuchadas</h2>
    <!-- Tu contenido aquí -->
    <HorizontalContainer>
      {#each mockArtist as playlist}
        <!--Card-->
        <ThumbnailCard title="" img={playlist.portada} />
      {/each}
    </HorizontalContainer>
  </div>

  <div class="w-full mb-6">
    <h2>Canciones Favoritas</h2>
    <!-- Tu contenido aquí -->
    <div>
      {#each mockSongs.slice(0, 5) as song, idx}
        <SongCard {song} {idx} />
      {/each}
      <a href="" class="ml-2 underline text-muted-foreground text-sm" onclick={(e) => { e.preventDefault(); ui.handleDialog(); }}
        >Ver Mas</a
      >
    </div>
  </div>

  <div class="w-full mb-6">
    <h2>Artistas Favoritos</h2>
    <!-- Tu contenido aquí -->
    <HorizontalContainer>
      {#each mockArtist as playlist}
        <!--Card-->
        <ThumbnailCard title={playlist.title} img={playlist.portada} />
      {/each}
    </HorizontalContainer>
  </div>
</div>
