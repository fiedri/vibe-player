# Vibe

Reproductor de música para android

## Características / Features

| Español | English |
| --- | --- |
| Reproductor con estado persistente: play, pause, siguiente y anterior | Persistent player state: play, pause, next and previous |
| Modos de reproducción: aleatorio, repetir uno y repetir todo | Playback modes: shuffle, repeat one and repeat all |
| Vistas de inicio, playlists, álbumes y artistas | Home, playlists, albums and artists views |
| Playlists persistentes en SQLite| Persistent playlists in SQLite|
| Búsqueda y filtro en cada sección | Search and filter in every section |
| Notificación de medios nativa (MediaSession) con controles del sistema | Native media notification (MediaSession) with system controls |
| Restauración del estado al reabrir la aplicación | State restoration when reopening the app |

## Instalación / Installation

### Usuarios / Users

Descargá el último APK desde la sección [Releases](https://github.com/fiedri/vibe-player/releases) de GitHub.

Download the latest APK from the [Releases](https://github.com/fiedri/vibe-player/releases) section on GitHub.

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

[Vibe](https://github.com/fiedri/vibe-player) es software libre y de código abierto, licenciado bajo GPL-3.0-or-later.

[Vibe](https://github.com/fiedri/vibe-player) is free and open-source software, licensed under [GPL-3.0-or-later](LICENSE).

Ver el archivo [LICENSE](LICENSE) para más detalles.

See the [LICENSE](LICENSE) file for details.
