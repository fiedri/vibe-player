# Migración `Song` → `MediaFile` (datos crudos)

## Estrategia (leé esto primero)

1. **La store guarda `MediaFile[]` CRUDO** (tal cual viene del plugin). Los datos reales quedan disponibles para crecer.
2. **El id se normaliza a `string` UNA sola vez, en el borde** (`cargarBiblioteca`). NO en cada consumidor. Esto mantiene compatibles: SQLite `song_id TEXT`, `SvelteSet<string>` en favoritos, `Map.get(songId)` en playlists, `Set<string>` en deleteManySongs, `===` en SongCard.
3. **Los defaults viven en los componentes** (elección del usuario): título, artista, álbum, cover.
4. **PERO las stores que AGRUPAN** (`artist`, `albumes`) necesitan el valor resuelto ANTES de agrupar, o todo cae en un grupo `undefined`. Para eso: constantes compartidas (sección abajo), usadas por stores (para agrupar) y componentes (para render).
5. **Cache bump obligatorio** — la caché vieja tiene shape `Song` y rompería en silencio hasta 24h.

## Constantes compartidas (nuevo)

Crear en `src/lib/types/songs.ts` (o `$lib/utils.ts`):

```ts
export const UNKNOWN_TITLE = "Sin título";
export const UNKNOWN_ARTIST = "Artista desconocido";
export const UNKNOWN_ALBUM = "Álbum desconocido";
export const DEFAULT_COVER = "/default-cover.png";

// Helpers de presentación (los usa render y agrupación por igual)
export function displayTitle(m: MediaFile) {
  return m.title || m.displayName || UNKNOWN_TITLE;
}
export function displayArtist(m: MediaFile) {
  return m.artist || UNKNOWN_ARTIST;
}
export function displayAlbum(m: MediaFile) {
  return m.album || UNKNOWN_ALBUM;
}
export function displayImage(m: MediaFile) {
  return m.albumArtUri || DEFAULT_COVER;
}
```

El `displayName` de `MediaFile` es el mejor fallback de título (hoy solo lo usa `formatbiblioteca`).

---

## FASE 1 — Tipos

### `src/lib/types/songs.ts`
- Reemplazar `Song` por `MediaFile` (mover la interfaz acá desde `files.ts` o re-exportarla).
- Eliminar `Song`. Campos que cambian: `artists`→`artist`, `audioUrl`→`uri`, `image`→`albumArtUri`, `id: string`→`id: string | number` (el tipo crudo — el string garantizado lo da el borde).
- Agregar constantes compartidas (arriba).

---

## FASE 2 — Servicios (borde de I/O)

### `src/lib/services/files.ts`
| Línea | Cambio |
|---|---|
| 33-54 | `interface MediaFile` → exportarla (o moverla a `types/songs.ts`) |
| 103-119 | `cargarBiblioteca`: normalizar id EN EL BORDE → `resultado.media.map(m => ({ ...m, id: String(m.id) }))` |
| 121-149 | Eliminar `formatbiblioteca` (los defaults pasan a componentes/constantes) |

Nota: `eliminarCancion`/`eliminarCanciones` ya reciben `uri` — no cambian.

---

## FASE 3 — Stores (capa de datos)

### `src/lib/stores/biblioteca.svelte.ts`
| Línea | Cambio |
|---|---|
| 21 | `Song[]` → `MediaFile[]` |
| 59-60, 113, 120 | Quitar call a `formatbiblioteca`; guardar `cargarBiblioteca()` directo |
| 89, 98 | dedupe por `s.id` — OK (id ya string) |
| 161 | `s.image` → `s.albumArtUri` |
| 176-181 | search: `s.title` → `displayTitle(s)`, `s.artists` → `displayArtist(s)`, `s.album` → `displayAlbum(s)` (así buscás "Artista desconocido" también) |
| 189 | `el.audioUrl` → `el.uri` |
| 200-203 | `el.id` OK, `el.audioUrl` → `el.uri` |
| 210 | `el.id` OK |

### `src/lib/stores/playlist.svelte.ts`
| Línea | Cambio |
|---|---|
| 18 | `Song[]` → `MediaFile[]` |
| 68 | `Map.get(pSong.songId)` — CRÍTICO: funciona SOLO si el id quedó string (Fase 2). Verificá con un test mental: id number → playlist vacía sin error |
| 57, 95 | firmas `songId: string` — OK gracias a normalización |

### `src/lib/stores/favorites.svelte.ts`
- `SvelteSet<string>`, firmas `songId: string` — OK si id normalizado. Verificar que todos los callers pasen `song.id` (string).

### `src/lib/stores/albumes.svelte.ts`
| Línea | Cambio |
|---|---|
| 20-40 | Agrupar por `displayAlbum(s)` ANTES de agrupar; `song.artists` → `song.artist`; `song.image` → `displayImage(s)` |
| 56 | `el.album` → `displayAlbum(el)` |

### `src/lib/stores/artist.svelte.ts`
| Línea | Cambio |
|---|---|
| 20-38 | Agrupar por `displayArtist(s)` ANTES de agrupar; `song.image` → `displayImage(s)` |
| 47-49 | `el.artists` → `displayArtist(el)` |

---

## FASE 4 — Subsistema player

### `src/lib/services/player/PlayerFacade.ts`
| Línea | Cambio |
|---|---|
| 33 | centinela `"/default-cover.png"` → `DEFAULT_COVER` |
| 45-62 | `song.image` → `displayImage(song)` |
| 67 | setter tipo `Song` → `MediaFile` |
| 152-153 | `el.id == lastState.trackId` — `==` loose, OK |
| 163 | `restoredSong.image` → `displayImage(restoredSong)` |
| 169 | `duration` — OK (MediaFile lo tiene en ms) |
| 192, 216, 237, 261 | firmas `Song` → `MediaFile` |

### `src/lib/services/player/subsystem/queue.svelte.ts`
| Línea | Cambio |
|---|---|
| 10-11, 15-16 | `Song[]` → `MediaFile[]` |
| 46 | `?.id == item.id` — OK |
| 61 | `song.id !== currentSong.id` — OK (ambos string) |
| 93-94 | `?.image` → `?.albumArtUri` (o `displayImage`) |

### `src/lib/services/player/subsystem/mediaSessionService.ts`
| Línea | Cambio |
|---|---|
| 65-75 | Objeto hardcodeado `init()`: `artists:""` → `artist:""`, `image` → `albumArtUri`, `audioUrl` → `uri`, `id:"default"` OK |
| 79-118 | `setMetadata`: `song.artists` → `song.artist`; simplificar branch `Array.isArray(song.artists)` (legacy, muere); ya tiene fallbacks propios — usá las constantes compartidas |
| 136-153 | `song?.duration` — OK |

### `src/lib/services/player/states/modeState.ts`
- Solo plumbing de tipos (`Song | null` → `MediaFile | null`). Mecánico.

---

## FASE 5 — Componentes (defaults acá)

### `src/lib/components/ui/Cards/SongCard.svelte`
| Línea | Cambio |
|---|---|
| 20-26 | props `Song` → `MediaFile` |
| 38-39 | `currentSong?.id === song.id` — OK si id string en ambos |
| 101 | `{song.title}` → `{displayTitle(song)}` |
| 104 | `{song.artists}` → `{displayArtist(song)}` |
| 111-112 | `song.duration` — OK |
| 182-183 | `song.audioUrl` → `song.uri` |
| 156, 169, 170 | `song.id` — OK |

### `src/lib/components/ui/player/player.svelte`
| Línea | Cambio |
|---|---|
| 159-160 | `audioUrl` → `uri` |
| 197, 199 | `image` → `displayImage(...)` |
| 205-207, 313-315 | onerror `"/default-cover.png"` → `DEFAULT_COVER` |
| 213, 217, 321, 326, 332 | `artists` → `displayArtist(...)`, `title` → `displayTitle(...)`, `album` → `displayAlbum(...)` |
| 280, 282 | `id` — OK |

### `src/lib/components/ui/menus/playerMenu.svelte`
| Línea | Cambio |
|---|---|
| 12 | `song.id` — OK |
| 20 | `song.audioUrl` → `song.uri` |

### `src/lib/components/ui/dialogs/confirmDeleteDialog.svelte`
| Línea | Cambio |
|---|---|
| 8 | `Set<string \| number>` → puede simplificarse a `Set<string>` |
| 25 | `Array.from(idsToDelete, String)` — ya defensivo, puede quedar o quitarse |

---

## FASE 6 — Rutas

| Archivo | Línea | Cambio |
|---|---|---|
| `src/routes/+layout.svelte` | 58-62, 102 | `currentSong?.id` — OK (acepta `string\|number`) |
| `src/routes/+layout.ts` | 16-17 | `biblioteca.songs = cache` — verificar shape tras cache bump |
| `src/routes/(app)/+page.svelte` | 16 | `el.id` — OK |
| `src/routes/(standalone)/album/[name]/+page.svelte` | 16, 107 | duration y key — OK |
| | 68 | `? .image` → `displayImage(...)` |
| | 74 | centinela → `DEFAULT_COVER` |
| `src/routes/(standalone)/artist/[name]/+page.svelte` | 77-82 | passthrough — OK |
| `src/routes/(standalone)/search/+page.svelte` | 100 | key — OK |
| `src/routes/(standalone)/playlist/[id]/+page.svelte` | 33, 39 | id, duration — OK |
| | 68 | `? .image` → `displayImage(...)` |
| | 109 | centinela → `DEFAULT_COVER` |

---

## FASE 7 — Persistencia (NO TE LO SALTEES)

### `src/lib/services/stores.ts`
- **Cache bump**: key `biblioteca_cache` → `biblioteca_cache_v2` (y `biblioteca_cache_timestamp` → `..._v2`). La caché vieja con shape `Song` se cargaría como `MediaFile` y rompe en silencio hasta 24h.

### `src/db/db/querys.ts` + `src/db/db/schema.ts`
- Firmas `songId: string` — quedan IGUAL gracias a la normalización de id en el borde. Verificar `removeSongFromAllPlaylists` y `removeManySongFromAllPlaylists` reciban string.

### `src/db/backup.ts`
- `songIds: string[]` — OK, solo ids.

### `src/lib/services/artworks.ts`
| Línea | Cambio |
|---|---|
| 202 | centinela `"/default-cover.png"` → `DEFAULT_COVER`. El short-circuit de `ensureThumbnail` depende de que el fallback llegue ANTES de llamarla (no le pases `albumArtUri` undefined crudo). |

---

## Verificación final

```bash
# 1. No debe quedar NINGÚN resto del tipo viejo
grep -rn "audioUrl" src/
grep -rn "\.artists\b" src/
grep -rn "formatbiblioteca" src/

# 2. Los únicos ".image" permitidos: helpers displayImage / albumArtUri
grep -rn "\.image\b" src/

# 3. Build + probar en device con la app DESINSTALADA o caché borrada
#    (sino la caché v1 con shape Song te va a confundir)
```

## Orden recomendado

1. FASE 1 (tipos + constantes) → 2. FASE 2 (borde) → 3. FASE 3 (stores) → 4. FASE 4 (player) → 5. FASE 5 (componentes) → 6. FASE 6 (rutas) → 7. FASE 7 (persistencia) → verificación.

Cada fase compila independiente SIEMPRE que hagas las fases anteriores. No toques la 7 antes de terminar las demás.