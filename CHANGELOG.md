# Changelog

Todas las novedades y cambios notables de **Vibe** se documentarán en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) y este proyecto se adhiere a [Semantic Versioning](https://semver.org/lang/es/).

---
## [Unreleased]

## [0.2.0] - 2026-08-04

### Added
- Opción para actualizar la biblioteca de medios manualmente.
- Submenú de opciones por canción (para acciones rápidas como agregar a playlists).
- Soporte completo para playlists:
  - Agregar y quitar canciones.
  - Crear y eliminar playlists.
  - Reproducción dedicada de playlists.

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
