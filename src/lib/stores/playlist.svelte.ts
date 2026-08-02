import { getPlaylists } from "$lib/db/db/querys";
class playlist{
  isLoading = $state<boolean>(false)
  isLoaded = $state<boolean>(false)
  playlists = $state<any[]>([]);
  error = $state<string | null>(null);

  public async loadPlaylist(){
    this.isLoading = true
    const result = await getPlaylists()
    this.playlists = result
    if(this.playlists){
      this.error = "No hay playlist Disponibles"
    }
  }
}

export const playlistStore = new playlist()
