# Changelog para Desarrolladores

> Historial técnico de `Vibe` a partir de los commits (Conventional Commits, SemVer).
> Para el usuario final ver [CHANGELOG.md](./CHANGELOG.md).
---

## [0.3.0] - 2026-08-09

**Commits:** `refactor(player): patrón State para modos de reproducción` (commit actual, rama `refactor/player-facade`)

### Contexto para devs

- **Patrón State** implementado para los modos de repetición: nuevo directorio `src/lib/services/player/states/` con `ModeState` (abstracto) y las implementaciones `RepeatOffmode`, `RepeatOneMode`, `RepeatAllMode`.
- `QueueManager.transitionTo(state)` delega `next()`, `previous()` y `handleTrackEnded()` al modo activo.
- `PlayerFacade.switchMode(mode)` cambia de modo (off → one → all).

### Archivos

- **Nuevos:** `src/lib/services/player/states/` (`modeState.ts`, `index.ts`).
- **Modificados:** `src/lib/services/player/PlayerFacade.ts`, `src/lib/services/player/subsystem/queue.svelte.ts`, `src/lib/components/ui/player/player.svelte`.

---
**Commits:** `9a621fe` `refactor(player): migrar lógica del reproductor al patrón Facade`

### Contexto para devs

- **Patrón Facade** aplicado al reproductor: toda la lógica se centraliza en `PlayerFacade`. Se eliminó el store legacy `playerStore.svelte.ts` (~419 líneas); ahora todo pasa por el facade.
- `PlayerFacade` expone el estado (`currentSong`, `isPlaying`, `volume`, `mode`, `currentIndex`, `numberOfSongs`, `playTrigger`) y delega en subsistemas: `AudioEngine` (audio/volumen), `QueueManager` (cola/shuffle), `ArtworkService` (artwork) y `MediaSessionService` (integración con controles nativos).
- El estado de UI (player abierto/cerrado) vive en `stores/ui.svelte.ts`.
- **Consecuencia:** la UI ya no toca el `<audio>` directamente; reacciona al `playTrigger` del facade.

### Archivos

- **Nuevos:** `src/lib/services/player/PlayerFacade.ts` y subsistemas `src/lib/services/player/subsystem/`.
- **Eliminados:** `src/lib/components/ui/player/playerStore.svelte.ts`.

---

- Migracion completa a carbon-icons
- Eliminacion de canciones del almacenamiento mediante un custom plugin en capacitor `MediaDeletePlugin`
- Seleccion multiple de canciones (long Press).

## [0.2.4] - 2026-08-05
- **Bug**: top-level await en `querys.ts`, cualquier modulo que lo importase se queda colgado hasta que termina la funcion `getDb()`
- **Fix**: convertir `getDb()` en una funcion lazy con reintentos y manejos de errores e ejecutarla en cada query

## [0.2.3] - 2026-08-05

**Commits:** `fix(playlist): dedupe por id evita each_key_duplicate (pantalla 400/404)`

### Contexto para devs

- **Bug (raíz):** al abrir una playlist que contenía canciones duplicadas, la app mostraba una pantalla de error que se percibía como "400/404 not found". No había ningún `error(400)` ni `fetch` en la app: era el **error de render de Svelte `each_key_duplicate`** ("Keyed each block has duplicate key `1000424350` at indexes 0 and 1"). SvelteKit lo muestra como página de error y en el WebView de Capacitor se ve como un "400 not found".
- Causa real: `biblioteca.songs` podía contener **dos objetos con el mismo `id`** (`String(id)` del MediaStore). `#mergeSongs` y la carga de caché dedupeaban por `audioUrl`, NO por `id`, así que dos filas con el mismo id pero distinta URI sobrevivían. `getArraySong` filtraba `biblioteca.songs` por id y devolvía ambas → el `{#each songs as song, idx (song.id)}` explotaba.
- **Fix (2 capas):** dedupe por `id` en `#mergeSongs` + carga de caché (helper `#dedupePorId`) como cura de raíz, y dedupe por `id` en `getArraySong` como defensa en el punto de consumo. Aunque el origen se cure, si la caché viejita ya tiene duplicados, `getArraySong` los filtra igual.
- **Bug secundario real:** `loadLastSavedState` llamaba `restoredSong.duration?.split(":")` pero `Song.duration` es un **número de ms** del MediaStore (no string). Era un crash latente que explotaba al arrancar con estado guardado: `duration?.split is not a function`. `updatePositionState` espera **segundos** (verificado: `player.duration = audioElement.duration`, `formatearMS(player.duration * 1000)`), así que ahora se convierte `duration/1000`.
- **Reproducción determinista (útil si vuelve):** inyectar un duplicado en `biblioteca.songs` (mismo id) y abrir la playlist que lo contenga. En la rama `repro/duplicate-playlist-400` quedó `injectDuplicateRepro()` + hooks `[REPRO400]`, descartados al mergear.

### Archivos

- **Modificados:** `src/lib/stores/biblioteca.svelte.ts` (`#mergeSongs`, `#dedupePorId`, carga de caché), `src/lib/stores/playlist.svelte.ts` (`getArraySong`), `src/lib/components/ui/player/playerStore.svelte.ts` (`loadLastSavedState`), `package.json`, `android/app/build.gradle` (0.2.3)

### Verificar

1. Reproducir el escenario: una playlist con la misma canción agregada dos veces (con duplicados previos en caché) debe abrirse normal, sin pantalla de error.
2. Cerrar la app con una canción reproduciéndose y reabrir: no debe aparecer `duration?.split is not a function` al restaurar el estado.

---

## [0.2.2] - 2026-08-04

**Commits:** `chore: bump a versión 0.2.2 (versionCode 3)` · `feat(playlist): backup y restauración de playlists en JSON` · `fix: 500 en instalación limpia desactivando el auto-backup de SQLite`

### Contexto para devs

- **Bug (raíz):** en instalación limpia la app crasheaba al abrir con `CapacitorSQLitePlugin: null`. El "500 Internal Error" era la **página de error de SvelteKit**: el `load` del `+layout` (que llama `getDb()` y `playlistStore.loadPlaylist()`) rechazaba.
- Causa real: bug conocido de `@capacitor-community/sqlite` ([#494](https://github.com/capacitor-community/sqlite/issues/494), [#603](https://github.com/capacitor-community/sqlite/issues/603)). **Android Auto-Backup/Restore** restaura datos viejos del plugin tras desinstalar/reinstalar en estado inconsistente. Por eso update (0.1.2→0.2.0) funcionaba y la reinstalación limpia no.
- **Fix:** `android:allowBackup="false"` + `android:fullBackupContent="false"` + nuevo `dataExtractionRules.xml` (excluye todo). En Android 12+ (`targetSdk=36`, `minSdk=24`) `allowBackup=false` solo no alcanza.
- **Consecuencia:** las playlists ya no se restauran solas. Se compensa con backup/restore manual en JSON (`src/lib/db/backup.ts`) usando el drizzle `db` (no el export/import del plugin, que recrea tablas). UI: `DialogType.Backup` + `backupPlaylistDialog.svelte` + botón `Download`.

### Archivos

- **Nuevos:** `android/app/src/main/res/xml/data_extraction_rules.xml`, `src/lib/db/backup.ts`, `src/lib/components/ui/dialogs/backupPlaylistDialog.svelte`
- **Modificados:** `AndroidManifest.xml`, `package.json`, `android/app/build.gradle` (0.2.2, versionCode 3), `src/lib/stores/ui.svelte.ts`, `src/routes/+layout.svelte`, `src/routes/(app)/playlist/+page.svelte`

### Verificar

1. Reconstruir APK (`pnpm build:android` → `cap sync` ya copió el manifest).
2. **Desinstalar completo** y reinstalar el 0.2.2 (instalar "encima" puede arrastrar data vieja).
3. Flujo: Exportar → guardás JSON → restaurás pegándolo.

---

## [0.2.0] - 2026-08-04

**Commits:** bump a 0.2.0 · docs de la versión · vista individual y gestión de canciones · creación y eliminación de playlists · vista condicional de playlists y formulario · botón de actualización manual de canciones · readme y número de versión

### Notas técnicas

- Feature principal: **playlists** persistidas en SQLite nativo (`@capacitor-community/sqlite` vía drizzle). Schema en `src/lib/db/db/`.
- **Top-level `await getDb()` en `src/lib/db/db/querys.ts`:** importar cualquier módulo que dependa de `querys` inicializa SQLite en el camino crítico del boot (fue el vector del bug 0.2.2). **Pendiente de refactor** a inicialización perezosa (lazy). 

---

## [0.1.2] - 2026-08-01

**Commit:** `fix: prev/next usa el índice de la cola en vez del de la biblioteca`

---

## [0.1.0 / 0.1.x] - 2026-07-31

**Commits (resumen):**
- `fix` doble audio focus + `perf`: mediastore N+1, paginación real, caché 24h y thumbnails en worker
- `fix` notificación MediaSession sin datos stale + menú de instrucciones OEM
- `feat`: vistas inicio/playlist/álbumes/artistas, modos de reproducción, persistencia de playlists
- `feat`: next/previous y notificaciones con control · renderizado virtual y caché · reproductor y lista de canciones (inicial)

---

## Deuda técnica / conocidos

- `svelte-check` reporta 27 errores previos, ajenos a este cambio: `(app)/+page.svelte` y `(app)/songs/+page.svelte` usan `<SongCard>` sin `playlistId`; param `playload` tipado `any` en `ui.svelte.ts`; dialog.svelte sin a11y; snippet `dialogType` con tipo implícito en `+layout.svelte`.
- Refactor pendiente: sacar el `await getDb()` top-level de `querys.ts` a inicialización lazy.
