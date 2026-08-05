# Changelog para Desarrolladores

> Historial técnico de `Vibe` a partir de los commits (Conventional Commits, SemVer).
> Para el usuario final ver [CHANGELOG.md](./CHANGELOG.md).

---

## [0.2.2] - 2026-08-04

**Commits:** `chore: bump a versión 0.2.2 (versionCode 3)` · `feat(playlist): backup y restauración de playlists en JSON` · `fix: 500 en instalación limpia desactivando el auto-backup de SQLite`

### Contexto para devs

- **Bug (raíz):** en instalación limpia la app crasheaba al abrir con `CapacitorSQLitePlugin: null`. El "500 Internal Error" era la **página de error de SvelteKit**: el `load` del `+layout` (que llama `getDb()` y `playlistStore.loadPlaylist()`) rechazaba.
- Causa real: bug conocido de `@capacitor-community/sqlite` ([#494](https://github.com/capacitor-community/sqlite/issues/494), [#603](https://github.com/capacitor-community/sqlite/issues/603)). **Android Auto-Backup/Restore** restaura datos viejos del plugin tras desinstalar/reinstalar en estado inconsistente. Por eso update (0.1.2→0.2.0) funcionaba y reinstalación limpia no.
- Pistas falsas descartadas: el cambio de tipo `duration: string → number` (es TS, se compila y desaparece) y las firmas de APK (darían `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, no 500).
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