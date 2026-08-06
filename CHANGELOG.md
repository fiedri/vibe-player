# Changelog

Todas las novedades y cambios notables de **Vibe** se documentarán en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) y este proyecto se adhiere a [Semantic Versioning](https://semver.org/lang/es/).

---
## [Unreleased]
- Dialog para mostrar errores al usuario en tiempo de ejecucion

## [0.2.3] - 2026-08-05

### Fixed
- Error al abrir algunas playlists que contenían la misma canción repetida (aparecía una pantalla de error al entrar). Las canciones repetidas ahora se filtran correctamente y la playlist abre normal.
- Error al reabrir la app con una canción en reproducción: el estado del reproductor ya no falla al restaurarse.

## [0.2.2] - 2026-08-04

### Fixed
- Error al abrir Vibe tras una **instalación limpia** en algunos dispositivos Android (pantalla de error en lugar del reproductor). La causa era el auto-backup de Android: al reinstalar, restauraba el estado interno del almacenamiento local en una condición rota. Se desactiva el backup para evitarlo.

### Added
- **Backup y restauración de playlists**: exportá tus listas a un JSON (se copia al portapapeles) y volvé a importarlas pegándolo después. Útil al reinstalar la app o cambiar de dispositivo, ya que el auto-backup quedó desactivado.

## [0.2.0] - 2026-08-04

### Added
- Opción para actualizar la biblioteca de medios manualmente.
- Submenú de opciones por canción (para acciones rápidas como agregar a playlists).
- Soporte completo para playlists:
  - Agregar y quitar canciones.
  - Crear y eliminar playlists.
  - Reproducción dedicada de playlists.

### Changed
- La cola de reproducción ahora restringe la navegación al contexto actual (playlist o biblioteca global).

## [0.1.2] - 2026-08-01

### Fixed
- Navegación `prev` / `next` en modo aleatorio ignoraba el orden real de la cola.

## [0.1.1] - 2026-07-31

### Added
- Notificación al pulsar botones con funcionalidades aún no disponibles.

## [0.1.0] - 2026-07-31

### Added
- Filtro de búsqueda individual para cada sección.
- Renderizado virtual para listas largas de canciones y carga en caché.
- Reproductor con estado persistente, controles (`play`, `pause`, `next`) y modos de reproducción (`shuffle`, `repeat-one`, `repeat-all`).
- Vistas principales: Inicio, Playlists, Álbumes, Canciones y Artistas.
