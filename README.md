# Vibe

An offline music player for Android. No account, no streaming backend, no network —
Vibe plays the audio files that are already on your phone.

> [!IMPORTANT]
> **This branch is the native Android rewrite, in Kotlin and Jetpack Compose.**
>
> Vibe used to be a SvelteKit web app wrapped in Capacitor. That version is frozen
> and lives on the `master` and `dev` branches, where it stays as a **behavioral
> reference** — it is not accepting new features.
>
> **To contribute, target this branch (`feat/migrate-to-native`).** Most of the app
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

- The Compose design system — 11 color tokens across light and dark, a real
  Inter variable font registered at six weights, and six custom typography slots.
- The four library screen layouts, with reusable `SongCard` and `ThumbnailCard`
  components.
- The top bar's animated tab indicator, which measures real tab positions.
- The player UI, including a hand-drawn seek bar with working tap and drag gestures.

**What is not started at all:** the entire data and playback spine.

| Area | State |
| --- | --- |
| Project setup — AGP 9.4.1, Gradle 9.6, Kotlin 2.2.10, Compose BOM 2026.02.01 | Done |
| Theme — colors and typography | Done, with a known bug (below) |
| Theme — shapes, dynamic color | Not started |
| Navigation shell and bottom bar | Done — manual `HorizontalPager` tab switching |
| Library screens — songs, albums, artists, playlists | Layouts done, **placeholder data** |
| Player screen | **Visual only, no audio behind it** |
| Reading the device library (MediaStore) | Not started — no permissions declared at all |
| Database | Not started — `room-ktx` is on the classpath but KSP is not configured, so no `@Entity` can compile |
| ViewModels, state holders, DI | Not started |
| Translations | Not started — one string in `strings.xml`, the launcher label |
| Tests | The two generated stubs, nothing real |

The play button does not make sound. The seek bar can be dragged, but the position it
reports is frozen. The manifest declares zero permissions.

See the [ROADMAP](ROADMAP.md) for the ordered plan.

## Known issues

Small, self-contained, and perfect for a first contribution. All of these are
confirmed bugs, not opinions:

- **The play/pause icon is inverted in the mini player.** `Player.kt` picks `PlayArrow`
  when playing in the mini player, and `Pause` when playing in the expanded player.
- **The theme ignores light mode.** `Theme.kt` hardcodes the dark color set for
  `VibeTheme.colors` while `MaterialTheme.colorScheme` does respect `darkTheme`, so in
  system-light mode Material components render light and the custom tokens stay dark.
- **Mini player previous/next are empty callbacks.** The parameters are threaded in
  and then dropped.
- **Dead buttons:** both playlist FABs, the top bar menu/search/overflow, and the
  player share/favorite/more buttons all have `onClick = {}`.
- **The playlists empty state is unreachable**, because the placeholder list is never
  empty.
- **`MainActivity.kt` has 22 unused imports** left over from when the shell lived there.
- **`res/values/colors.xml` holds 7 unused template colors** from the project wizard.
- **`keepRules/rules.keep` is entirely commented out**, and R8 is currently disabled
  for release builds.


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
| Playlists, stored in SQLite | Yes | No |
| Favorites | Yes | No |
| Search and filter per section | Yes | No |
| Sorting options | Yes | No |
| Multi-select, "play next" | Yes | No |
| Share and delete a song | Yes | No |
| Language switcher (en, es) | Yes | No |

## Tech stack

- **Kotlin 2.2.10** with **Jetpack Compose** (BOM 2026.02.01) and **Material 3**
- **AGP 9.4.1**, **Gradle 9.6.0**, configuration cache enabled
- `minSdk 24`, `targetSdk 37`, `compileSdk 37`
- `versionName 0.8.0`, `versionCode 13`
- Gradle daemon on **JDK 25**, auto-provisioned through the foojay resolver
- Inter variable font, registered across six weights via font variation settings
- Declared but not yet wired: **Room**, **Navigation 3**, kotlinx-serialization
- Planned: **Media3 / ExoPlayer** for audio, **MediaStore** for the device library

## Getting started

### With Android Studio

1. Clone the repository and check out this branch:
   ```bash
   git clone https://github.com/fiedri/vibe-player.git
   cd vibe-player
   git checkout feat/migrate-to-native
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

## Project layout

```
app/src/main/java/dev/fiedri/vibe/
├── MainActivity.kt        # single activity, Compose entry point
├── player/Player.kt       # player screen composables
└── ui/
    ├── VibeApp.kt         # root composable, navigation shell
    ├── components/        # reusable composables
    ├── screen/            # one file per screen
    └── theme/             # colors, typography, shapes
```

Screen and component files are lowercase by convention (`songs.kt`, not `Songs.kt`).
Styling comes from the theme tokens, never from inline hex values and font sizes — see
[CONTRIBUTING.md](CONTRIBUTING.md) for the rules.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. In short:
target this branch, keep PRs small, and use [Conventional Commits](https://www.conventionalcommits.org/).

Everyone participating is expected to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

Vibe is free and open-source software, licensed under
[GPL-3.0-or-later](LICENSE).

See the [LICENSE](LICENSE) file for details.
