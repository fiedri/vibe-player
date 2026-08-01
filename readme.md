# Vibe

Reproductor MP3 local para Android construido con Svelte 5 y Capacitor 8. Reproduce archivos MP3 almacenados en el dispositivo, permite buscar canciones y controlar la reproducción desde la notificación y la sesión de medios del sistema.

Local MP3 player for Android built with Svelte 5 and Capacitor 8. Plays MP3 files stored on the device, supports song search, and playback control from the notification and the system media session.

## Características / Features

| Español | English |
| --- | --- |
| Reproductor con estado persistente: play, pause, siguiente y anterior | Persistent player state: play, pause, next and previous |
| Modos de reproducción: aleatorio, repetir uno y repetir todo | Playback modes: shuffle, repeat one and repeat all |
| Vistas de inicio, playlists, álbumes y artistas | Home, playlists, albums and artists views |
| Playlists persistentes en SQLite (creación y edición próximamente) | Persistent playlists in SQLite (creation and editing coming soon) |
| Búsqueda y filtro en cada sección | Search and filter in every section |
| Renderizado virtual y caché de canciones | Virtual rendering and song caching |
| Notificación de medios nativa (MediaSession) con controles del sistema | Native media notification (MediaSession) with system controls |
| Restauración del estado al reabrir la aplicación | State restoration when reopening the app |

## Instalación / Installation

### Usuarios / Users

Descargá el último APK desde la sección [Releases](https://github.com/fiedri/Vibe/releases) de GitHub.

Download the latest APK from the [Releases](https://github.com/fiedri/Vibe/releases) section on GitHub.

### Desarrollo / Development

Tecnologías utilizadas / Technologies used:

| Tecnología / Technology | Propósito / Purpose |
| --- | --- |
| Svelte 5 | Framework UI |
| Capacitor 8 | Runtime nativo / Native runtime |
| SQLite (Drizzle ORM) | Persistencia local / Local persistence |
| Tailwind CSS 4 | Estilos / Styling |

Pasos para instalar dependencias y probar / Steps to install dependencies and run:

```bash
# Instalar dependencias / Install dependencies
pnpm install

# Probar en el navegador / Run in the browser
pnpm dev

# Sincronizar cambios web al proyecto Android / Sync web changes to the Android project
npx cap sync android

# Abrir el proyecto en Android Studio / Open the project in Android Studio
npx cap open android
```

Para generar el APK, usar Android Studio (Build > Generate Signed Bundle / APK) o Gradle desde la carpeta `android/`.

To build the APK, use Android Studio (Build > Generate Signed Bundle / APK) or Gradle from the `android/` folder.

## Capturas / Screenshots

Pendiente / Pending.

## Licencia / License

Sin licencia especificada. Todos los derechos reservados.

No license specified. All rights reserved.
