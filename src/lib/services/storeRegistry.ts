import { albumes } from '$lib/stores/albumes.svelte';
import { artists } from '$lib/stores/artist.svelte';
import { biblioteca } from '$lib/stores/biblioteca.svelte';
import type { SortableStore } from '$lib/types/sortable';

export function getSortableStoreByPath(path: string): SortableStore | null {
  if (path === '/') return biblioteca;
 if(path.startsWith('/albums')) return albumes
   if(path.startsWith('/artist')) return artists
  return null;
}
