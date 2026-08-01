# CHANGELOG

## Vibe

Reproductor MP3 local para Android construido con Svelte 5 + Capacitor 8. Reproduce archivos MP3 almacenados en el dispositivo, permite crear playlists, buscar canciones y controlar la reproducción desde notificaciones y la sesión de medios del sistema.

---

## [0.1.0] - 2026-07-31

### Added

- Implementación de filtro de búsqueda para cada sección
- Renderizado virtual de canciones y carga en cache
- Reproductor funcional con estado persistente, botones play, next y pause, y modos de reproducción aleatorio, repeatone y repeatall
- Vistas de inicio, playlist, álbumes y artistas, conexión a sqlite para guardado de las playlist

## [0.1.1] - 2026-07-31

### Added
- Notificación en botones de funcionalidades no disponibles en la versión actual

## [0.1.2] - 2026-08-01

### Fixed
- navegación prev/next en modo aleatorio ignoraba el orden de la cola
