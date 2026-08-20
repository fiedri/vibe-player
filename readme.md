

https://github.com/user-attachments/assets/bb9c6b3d-9a11-46e1-8010-32d19ab609bc

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

* **ES:** Descargá el último APK desde la sección [Releases](https://github.com/fiedri/vibe-player/releases).
* **EN:** Download the latest APK from the [Releases](https://github.com/fiedri/vibe-player/releases) section.

---

### Desarrolladores / Developers

#### Prerrequisitos / Prerequisites
* [Node.js](https://nodejs.org/) (>= 22.0.0)
* [pnpm](https://pnpm.io/) (recomendado / recommended) o npm

#### Pasos de instalación / Setup

```bash
# 1. Clona el repositorio / Clone repository
git clone https://github.com/fiedri/vibe-player.git

# 2. Navega al directorio / Navigate to directory
cd vibe-player

# 3. Instala dependencias / Install dependencies
pnpm install
```

### Ejecución y pruebas / How to run
- **Modo Web (Navegador) / Web Mode (Browser)**
```bash
pnpm run dev
```

- **Modo Desarrollo en Dispositivo / Live Reload on Device**:
* **ES:**Conectá tu teléfono por USB con depuración activa y ejecutá:
* **EN:**Connect your phone via USB with debugging enabled and run:
```bash
# Levanta el servidor/ Raise the server
pnpm run dev --host

# Sincronizar Capacitor y levantar en Android/ sync capacitor and open in Android
pnpm dev:android
```

- **Compilar e instalar en dispositivo / Build and run on Device:**
```bash
# Construye el frontend y sincroniza capacitor/ Build the frontend and synchronize capacitor
pnpm build:android

# Compila y ejecuta la app / Compile and run the app
npx cap run android
```

## Capturas / Screenshots
<p align="center">
  <img src="./fastlane/metadata/android/es-419/images/phoneScreenshots/1.jpeg" alt="Main screen" width="280">
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="./fastlane/metadata/android/es-419/images/phoneScreenshots/2.jpeg" alt="Playlist view" width="280">
    <img src="./fastlane/metadata/android/es-419/images/phoneScreenshots/4.jpeg" alt="Playlist view" width="280">
</p>

## Licencia / License

[Vibe](https://github.com/fiedri/vibe-player) es software libre y de código abierto, licenciado bajo GPL-3.0-or-later.

[Vibe](https://github.com/fiedri/vibe-player) is free and open-source software, licensed under [GPL-3.0-or-later](LICENSE).

Ver el archivo [LICENSE](LICENSE) para más detalles.

See the [LICENSE](LICENSE) file for details.
