# Changelog para Desarrolladores

> Historial técnico orientado a los commits y decisiones de `Vibe`.
> Para el usuario final ver [CHANGELOG.md](./CHANGELOG.md). Convenciones de commit: Conventional Commits. Versión: [SemVer](https://semver.org/lang/es/).

---

## [0.2.2] - 2026-08-04

### Commits

- `30f2019` `chore`: bump a versión 0.2.2 (versionCode 3)
- `8fb1d4d` `feat(playlist)`: backup y restauración de playlists en JSON
- `bd8d249` `fix`: 500 en instalación limpia desactivando el auto-backup de SQLite

### Qué pasó (contexto para devs)

- **Bug (raíz):** en instalación limpia la app crasheaba al abrir con `CapacitorSQLitePlugin: null`. Diagnóstico:
  - El "500 Internal Error" era la **página de error de SvelteKit**: el `load` del `+layout` (que llama `getDb()` y `playlistStore.loadPlaylist()`) rechazaba y SvelteKit renderizaba su pantalla genérica.
  - Causa real: bug conocido de `@capacitor-community/sqlite` ([#494](https://github.com/capacitor-community/sqlite/issues/494), [#603](https://github.com/capacitor-community/sqlite/issues/603)). **Android Auto-Backup/Restore** restaura datos viejos del plugin tras desinstalar/reinstalar en estado inconsistente → no se instancia el nativo. Por eso update (0.1.2→0.2.0) funcionaba y reinstalación limpia no.
  - Descartadas como pistas falsas: cambio de tipo `duration: string → number` (es TS, se compila y desaparece) y firmas de APK (darían `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, no 500).
- **Fix:** `android:allowBackup="false"` + `android:fullBackupContent="false"` + nuevo `dataExtractionRules.xml` (excluye todo). En Android 12+ (`targetSdk=36`, `minSdk=24`) `allowBackup=false` solo no es suficiente, requiere `dataExtractionRules`.
- **Consecuencia:** las playlists ya no se restauran solas al reinstalar. Se compensa con backup/restore manual en JSON (`src/lib/db/backup.ts`) usando el drizzle `db` (no el export/import del plugin, que recrea tablas y chocaría con la base ya creada en `initDatabase`). UI: `DialogType.Backup` + `backupPlaylistDialog.svelte` + botón `Download` en la vista de playlists.

### Archivos nuevos

- `android/app/src/main/res/xml/data_extraction_rules.xml`
- `src/lib/db/backup.ts`
- `src/lib/components/ui/dialogs/backupPlaylistDialog.svelte`

### Archivos modificados

- `android/app/src/main/AndroidManifest.xml`
- `package.json`, `android/app/build.gradle` (0.2.2, versionCode 3)
- `src/lib/stores/ui.svelte.ts`, `src/routes/+layout.svelte`, `src/routes/(app)/playlist/+page.svelte`

### Verificar

1. Reconstruir APK (`pnpm build:android` → `cap sync` ya copió el manifest).
2. **Desinstalar completo** y reinstalar el 0.2.2 (instalar "encima" puede arrastrar data vieja).
3. Flujo: Exportar → guardás JSON → restaurás pegándolo.

---

## [0.2.0] - 2026-08-04

### Commits

- `0d4de62` `chore`: bump a versión 0.2.0
- `55aec17` `docs`: descripción de la versión 0.2.0
- `2da2501` `feat(playlist)`: vista individual y gestión de canciones
- `ad8b657` `feat`: creación y eliminación de playlists
- `4d362b9` `feat`: vista condicional de playlists y formulario de creación
- `4d9edd7` `feat`: botón de actualización manual de canciones en la biblioteca
- `4d0a4a5` `docs`: readme y actualización del número de versión

### Notas técnicas

- Feature principal: **playlists** persistidas en SQLite nativo (`@capacitor-community/sqlite` vía drizzle). Schema en `src/lib/db/db/`.
- **Top-level `await getDb()` en `src/lib/db/db/querys.ts` (línea 5):** importar cualquier módulo que dependa de `querys` inicializa SQLite en el camino crítico del boot. Si SQLite falla, derriba el arranque (fue el vector del bug 0.2.2). **Pendiente de refactor** a inicialización perezosa (lazy) dentro de cada query — candidato para deuda técnica.

---

## [0.1.2] - 2026-08-01

- `2945aa0` `fix`: prev/next usa el índice de la cola en vez del de la biblioteca

---

## [0.1.x / 0.1.0] - 2026-07-31

- `c618aff` `feat`: diálogo de funciones no implementadas, página de error, changelog y roadmap (0.1.0)
- `83d8436` `feat(perf)`: filtro de búsqueda por sección y actualización de versión
- `30135ee` `fix` + `perf`: pausa por doble audio focus; mediastore N+1, paginación real, caché 24h y thumbnails en worker
- `4fa9bc1` `fix`: notificación MediaSession sin datos stale, thumbnails en caché, menú de instrucciones OEM
- `a96f06e` `feat`: vistas inicio/playlist/álbumes/artistas, modos de reproducción, persistencia de playlists en SQLite
- `3c03076` `feat`: next/previous del reproductor y notificaciones con control
- `e2d049c` `feat`: renderizado virtual de canciones y carga en caché
- `a0cbb28` `feat`: reproductor y lista de canciones (commit inicial)

---

## Deuda técnica / conocidos

- `svelte-check` reporta 27 errores previos, ajenos a este cambio:
  - `src/routes/(app)/+page.svelte`: usa `mockSongs` y `<SongCard>` sin la prop `playlistId`.
  - `src/routes/(app)/songs/+page.svelte`: `<SongCard>` sin `playlistId`.
  - `src/lib/stores/ui.svelte.ts`: param `playload` tipado como `any`.
  - `src/lib/components/ui/dialogs/dialog.svelte`: divs con onclick sin rol/a11y.
  - `src/routes/+layout.svelte`: parámetro del snippet `dialogType` con tipo implícito `any`.
- Refactor pendiente: extraer el `await getDb()` top-level de `querys.ts` a inicialización lazy.