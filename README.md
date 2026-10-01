# Vibe

An offline music player for Android. No account, no streaming backend, no network —
Vibe plays the audio files that are already on your phone.

> [!IMPORTANT]
> **This branch is the native Android rewrite, in Kotlin and Jetpack Compose.**
>
> Vibe used to be a SvelteKit web app wrapped in Capacitor. That version is frozen
> on the `legacy` branch, where it stays as a **behavioral
> reference** — it is not accepting new features.
>
> **To contribute, target `main` (the default branch).** Most of the app
> is not written yet, so there is a lot of room.

## Why rewrite it

The web version was held back by its own architecture, not by its logic:

- **Audio.** Decoding and playback ran through a JavaScript audio engine inside a
  WebView, behind a bridge. Even after bolting a native ExoPlayer plugin underneath,
  every state change crossed the JS↔Kotlin boundary.
- **Scrolling.** Long libraries went through a virtualized list in a DOM, then a
  native list. Composables skip the DOM entirely.
- **Background playback.** Reliable playback with a media notification is a
  first-class thing in Android, and fighting the WebView for it was most of the work.

None of that said the app was badly made — it was just the wrong tool for an audio
player on Android.

## Status

Being straight about where this is, because it is early and pretending otherwise
helps nobody.

**What is genuinely built and worth building on:**

- The Compose design system — 15 color tokens for light and dark, a real
  Inter variable font registered at six weights, and six custom typography slots.
- Navigation on **Navigation 3**, with a real back stack of six destinations and a
  hand-written `Navigator` behind a composition local.
- Ten screens: the four library tabs, album / artist / playlist detail, search,
  settings, and the player in both its mini and expanded form.
- The top bar's animated tab indicator, which measures real tab positions.
- The player UI, including a hand-drawn seek bar with working tap and drag gestures.
- Context menus: song options as a bottom sheet, a player dropdown, a home overflow
  menu with per-tab sorting, and a slide-in settings drawer.
- **156 strings in English, 155 in Spanish and 155 in Polish**, ported from the web
  app's message catalogs.

**What is not started at all:** the entire data and playback spine.

| Area | State |
| --- | --- |
| Project setup — AGP 9.4.1, Gradle 9.6, Kotlin 2.2.10, Compose BOM 2026.02.01 | Done |
| Theme — colors and typography | Done, with a known bug (below) |
| Theme — shapes, dynamic color | Not started |
| Navigation — Navigation 3 with a back stack | Done |
| Library screens — songs, albums, artists, playlists | Layouts done, **placeholder data** |
| Detail, search and settings screens | Layouts done, **placeholder data** |
| Player screen | **Visual only, no audio behind it** |
| Reading the device library (MediaStore) | Not started — no permissions declared at all |
| Database | Not started — no Room dependency, no KSP plugin, no entities |
| ViewModels, state holders, DI | Not started |
| Translations | Ported, 24 of 156 strings wired; no language switcher |
| Tests | The two generated stubs, nothing real |

The play button does not make sound. The seek bar can be dragged, but the position it
reports is frozen. The manifest declares zero permissions.

See the [ROADMAP](ROADMAP.md) for the ordered plan.

## What the web version does

For reference, this is the feature set being ported. The right-hand column is the
honest current state on this branch.

| Feature | Web version | Here |
| --- | --- | --- |
| Play, pause, next, previous | Yes | UI only |
| Shuffle, repeat one, repeat all | Yes | UI only |
| Persistent playback state | Yes | No |
| Media notification (MediaSession) | Yes | No |
| Songs / albums / artists views | Yes | Placeholder data |
| Playlists, stored in SQLite | Yes | List UI with placeholder data, no CRUD |
| Favorites | Yes | A hardcoded `"favoritos"` row, no storage |
| Search and filter per section | Yes | Search screen built, filters placeholder data |
| Sorting options | Yes | Menu built, callback not wired to anything |
| Multi-select, "play next" | Yes | No |
| Share and delete a song | Yes | Menu items rendered, handlers empty |
| Language switcher (en, es, pl) | Yes | No — translations exist, the switcher does not |

## Tech stack

- **Kotlin 2.2.10** with **Jetpack Compose** (BOM 2026.02.01) and **Material 3**
- **AGP 9.4.1**, **Gradle 9.6.0**, configuration cache enabled
- **Navigation 3** (`navigation3-ui` / `navigation3-runtime` 1.2.0) and
  **kotlinx-serialization** for the `@Serializable` nav keys
- `minSdk 24`, `targetSdk 37`, `compileSdk 37`
- `versionName 1.0.0`, `versionCode 13` — **neither is fit for a distribution channel yet**
- Gradle daemon on **JDK 25**, auto-provisioned through the foojay resolver
- Inter variable font, registered across six weights via font variation settings
- Translations in `values/`, `values-es/` and `values-pl/`
- Not yet in the project: Room, Hilt, Media3 / ExoPlayer, MediaStore, any ViewModel
- Planned: **Media3 / ExoPlayer** for audio, **MediaStore** for the device library

## Getting started

### With Android Studio

1. Clone the repository (`main` is the default branch):
   ```bash
   git clone https://github.com/fiedri/vibe-player.git
   cd vibe-player
   ```
2. Open the folder in Android Studio. Let it sync — Gradle will fetch the JDK 25
   daemon and the Android SDK platform it needs.
3. Run the `app` configuration on a device or emulator.

### From the command line

```bash
./gradlew :app:assembleDebug        # build
./gradlew :app:installDebug          # install on a connected device
./gradlew testDebugUnitTest          # unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (device required)
```

You need a `local.properties` with your SDK path if Android Studio has not created one
for you:

```properties
sdk.dir=/path/to/your/Android/sdk
```

If a build error mentions a type or package that you can see in your editor, run
`./gradlew clean` first. The `app/build/` directory survives branch switches and can
serve you stale classes from a previous layout.

## Project layout

```
app/src/main/java/dev/fiedri/vibe/
├── core/ui/
│   ├── MainActivity.kt       # single activity: VibeTheme { VibeApp() }
│   ├── VibeApp.kt            # composition root
│   ├── NavigationKeys.kt     # 6 @Serializable NavKey types
│   ├── navigation.kt         # Navigator(backStack) + VibeNavGraph
│   ├── composables/          # shared UI: CardGrid, DetailsScreen, SongCard,
│   │                         # ThumbnailCard, VibeMenu, VibeTopBar, Pager
│   │   └── models/           # CardData
│   └── theme/                # Color.kt, Theme.kt, Type.kt
└── features/<name>/presentation/
    ├── home/                 # Home.kt, HomeContentMenu.kt, HomeOptionsMenu.kt
    ├── songs/                # songs.kt + SongOptionsSheet
    ├── albums/               # albums.kt, albumDetails.kt
    ├── artists/              # artists.kt, artistDetails.kt
    ├── playlists/            # playlists.kt, playlistDetails.kt
    ├── player/               # Player.kt, PlayerMenu.kt, PlayerUiState.kt
    ├── search/               # search.kt
    └── settings/             # settingsScreen.kt
```

Screen file casing is **not** uniform: `songs.kt`, `albums.kt`, `artists.kt`,
`playlists.kt` and `search.kt` are lowercase, while `albumDetails.kt`,
`artistDetails.kt`, `playlistDetails.kt` and `settingsScreen.kt` are camelCase.
Component files are mostly PascalCase, with `detail.kt`, `songCard.kt`, `utils.kt` and
`cardDataModel.kt` lowercase in the same package. Match the file you are editing; do not
start a rename war.

Styling is *meant* to come from the theme tokens rather than inline hex values and raw
font sizes, and that is the rule new code must follow — but the existing tree violates it
in around 35 places. See [CONTRIBUTING.md](CONTRIBUTING.md) for the rules.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. In short:
target this branch, keep PRs small, and use [Conventional Commits](https://www.conventionalcommits.org/).

If you are new here, the
[first-time contributor issue template](.github/ISSUE_TEMPLATE/first_time_contributor.md)
leads with the biggest mechanical win in the repo: the translations already ship in
three locales, but around forty user-facing strings are still hardcoded in the Kotlin
source.

Everyone participating is expected to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

Vibe is free and open-source software, licensed under
[GPL-3.0-or-later](LICENSE).

See the [LICENSE](LICENSE) file for details.
