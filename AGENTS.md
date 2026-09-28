# AGENTS.md

Native Android music player ("Vibe Player"), Kotlin + Jetpack Compose. Branch
`feat/migrate-to-native`. Single Gradle module `:app`, namespace `dev.fiedri.vibe`.

It is a rewrite of a SvelteKit/Capacitor web app that lives in a **separate clone** at
`~/Documentos/Programacion/projects/vibe-player` (branches `master` / `dev`). That repo is
the behavioral reference — check its `src/`, `messages/` and `drizzle.config.ts` before
inventing behavior. Everything it lacks (data, playback) is absent here too, on purpose.

## Trust the code, not the prose

This file and the source tree are the authority. `README.md`, `CONTRIBUTING.md` and
`ROADMAP.md` are contributor-facing and drift fast; a restructure moves packages without
touching them. Verify any doc claim against the tree before repeating it.

When a restructure renames or moves a type, the compiler is the checklist. `a5aabbd`
moved `CardData` to `core.ui.composables.models` and `SongCardData` to `SongCardUiState`,
updated two of three consumers, and left `detail.kt`, `search.kt` and `playlistDetails.kt`
broken. `:app:assembleDebug` is the only thing that catches this.

`ARCHITECTURE.md` does not exist. Do not cite it. The layer triggers are below instead.

## Actual layout

```
app/src/main/java/dev/fiedri/vibe/
├── core/ui/
│   ├── MainActivity.kt          # 20 L, setContent { VibeTheme { VibeApp() } }
│   ├── VibeApp.kt               # composition root — MISPLACED, see the rule below
│   ├── NavigationKeys.kt        # 6 @Serializable NavKey types
│   ├── navigation.kt            # Navigator(backStack) + VibeNavGraph
│   ├── composables/
│   │   ├── GridCard.kt          # CardGrid + CardData grid
│   │   ├── ThumbnailsCard.kt    # ThumbnailCard
│   │   ├── VibeMenu.kt          # VibeMenu, VibeMenuItem  ← 10 unused imports
│   │   ├── VibeToBar.kt         # VibeTopBar (typo is in the filename)
│   │   ├── detail.kt            # DetailsScreen, DetailHeader, DetailResources, EntityType
│   │   ├── songCard.kt          # SongCard + SongCardUiState
│   │   ├── utils.kt             # Pager
│   │   └── models/cardDataModel.kt   # CardData
│   └── theme/                   # Color.kt, Theme.kt, Type.kt
└── features/<name>/presentation/
    ├── home/         Home.kt, HomeContentMenu.kt, HomeOptionsMenu.kt
    ├── songs/        songs.kt                      (+ SongOptionsSheet)
    ├── albums/       albums.kt, albumDetails.kt
    ├── artists/      artists.kt, artistDetails.kt
    ├── playlists/    playlists.kt, playlistDetails.kt
    ├── player/       Player.kt, PlayerMenu.kt, PlayerUiState.kt
    ├── search/       search.kt
    └── settings/     settingsScreen.kt
```

- There is no `ui/`, no `screen/` package, no `player/` at the root, and no `navigation/`
  package. All of those existed and were deleted by the restructures.
- `Screen` files are **not** uniformly lowercase. `songs.kt`, `albums.kt`, `artists.kt`,
  `playlists.kt`, `search.kt` are lowercase; `albumDetails.kt`, `artistDetails.kt`,
  `playlistDetails.kt`, `settingsScreen.kt` are camelCase; component files are mostly
  PascalCase with `detail.kt`, `songCard.kt`, `utils.kt`, `cardDataModel.kt` lowercase in
  the same package. Match the file you are editing; do not start a rename war.
- File name ≠ symbol in several places: `GridCard.kt`→`CardGrid`,
  `ThumbnailsCard.kt`→`ThumbnailCard`, `VibeToBar.kt`→`VibeTopBar`,
  `HomeContentMenu.kt`→`HomeMenu`, `HomeOptionsMenu.kt`→`SettingsDrawer`.

## Navigation is Navigation 3, not a pager

`core/ui/NavigationKeys.kt` declares 6 `@Serializable` `NavKey` types: `Home`,
`AlbumDetail(id)`, `ArtistDetail(name)`, `PlaylistDetail(id)`, `Search`, `Settings`.
`core/ui/navigation.kt:22-26` is a hand-written `Navigator(val backStack: MutableList<NavKey>)`
exposed through `LocalNavigator`; `navigation.kt:36-59` is a `NavDisplay` with an
`entryProvider`.

This means `navigation3-ui`, `navigation3-runtime` and `kotlinx-serialization-core` are all
**load-bearing**, not speculative. The `@Serializable` annotations do nothing without the
serialization plugin. Any doc claiming they are "declared but unused" is wrong.

The `HorizontalPager` in `Home.kt:65-107` is only the tab switcher between the four library
screens. It is not navigation.

## The dependency rule, and the three places it is broken

> `core` is shared infrastructure. Features may import `core`. `core` must never import
> `features`. The composition root may import both.

Nothing enforces this: one Gradle module, so Kotlin packages are naming convention only. The
IDE will happily offer the import and the build will pass. **Review is the enforcement
mechanism.** Three live violations, all present today:

- `core/ui/VibeApp.kt:16-19` imports four `features.player.presentation` symbols. The
  composition root is *still under `core/`*. Moving `VibeApp.kt` to `dev.fiedri.vibe/` is
  the fix, and it is the first thing to do before adding a second feature.
- `core/ui/navigation.kt:8,13-17` imports six feature symbols. Legitimate for a composition
  root, illegitimate at its current location — it moves together with `VibeApp.kt`.
- `core/ui/composables/VibeToBar.kt:34` imports `features.home.presentation.HomeMenu`, so
  the "shared by more than one feature and owned by none" package is not feature-agnostic.
  Either move the menu into `core` or push the trigger up into `HomeLayout`.

## Do not build layers before their trigger

The reflex to add a Repository + ViewModel + Hilt is wrong here — each is deferred until it
has something to do:

- **ViewModel + StateFlow** — first async data load (the first MediaStore query). The first
  state holder in the tree is `PlayerUiState`, a plain data class passed as a parameter.
- **Repository interfaces** — a second implementation, or a test fake. One impl = not abstraction.
- **DI** — start now, but hand-written `AppContainer` (~40 lines). Hilt at ~10 bindings.
- **Room** — first `@Entity`. Nothing Room-related is on the classpath today; `room-ktx`,
  `room-compiler` **and** the KSP plugin must land together or no entity can compile.
- **`:domain` JVM module** — when the domain model has real rules (favorites sentinel
  `"favoritos"`, queue order, playlist membership).
- **Media3 / ExoPlayer** — the play button is a `Boolean`. There is no audio engine at all.

Cosmetic layering is actively harmful: it makes a reader assume persistence, threading and
queue semantics are solved when they are the actual open risk.

## Conventions

- **Styling comes from `VibeTheme.colors` / `VibeTheme.typography`** (`staticCompositionLocalOf`).
  No `Color(0x…)` and no raw `.sp` outside `Color.kt` / `Type.kt`. This is the rule most often
  broken — see the debt list below.
- Commits: conventional commits, no AI attribution, no `Co-Authored-By`.

### Known debt — do not describe any of this as "not started"

- `VibeColors` has **15** tokens plus an `accent` alias. `VibeLightColors` is fully defined
  but **unreachable**: `Theme.kt:132` has the correct line commented out and `:133` hardcodes
  `val colors = VibeDarkColors`. `MaterialTheme.colorScheme` at `:141` still branches on
  `darkTheme`, so a light system theme produces Vibe tokens over a light M3 scheme.
- i18n is **partially adopted, not absent**: 156 strings in `values/`, 155 in `values-es/`,
  155 in `values-pl/`, plus 2 `<plurals>` per locale. Only 24 of 156 are wired to
  `stringResource`. Around forty user-facing strings are hardcoded in the Kotlin source
  across seven files — `Player.kt`, `playlists.kt`, `detail.kt`, `VibeToBar.kt`,
  `settingsScreen.kt`, `ThumbnailsCard.kt`, `songCard.kt`. Find them with
  `rg 'contentDescription\s*=\s*"|text\s*=\s*"|Text\("' app/src/main/java/`.
  Most replacement keys already exist and are wired to nothing: `menus_back`, `shuffle`,
  `play`, `songs_options_share`, `songs_options_delete`, `playlist_title`,
  `playlist_favorites`, `playlist_no_playlist`, `playlist_add_new_playlist`,
  `playlist_backup_export`, `menus_song_options`, `settings_general`,
  `menus_mainmenu_settings`.
- **`R.plurals` is referenced from zero places** and the counts are string-concatenated:
  `detail.kt:121` writes `if (size == 1) "1 cancion" else "$size canciones"` and `:135`
  writes `"$size albumes"`. Both are **grammatically wrong in Polish** — `values-pl` ships
  the correct `one`/`few`/`many`/`other` forms (`3 utwory` vs `5 utworów`) that
  concatenation cannot express. This is a real bug, not a cosmetic one.
- No language switcher exists, so there is no way to see the untranslated states without
  changing the device language. `values/` and `values-es/` carry "Auto-generated from
  messages/{en,es}.json" headers; `values-pl/` has no provenance comment and
  `../vibe-player/messages/` only holds `en`/`es`.
- No `Shape.kt`, no spacing tokens, so every `.dp` is a raw literal and components hardcode
  `RectangleShape` at 8 sites.
- `VibeTypography` has 6 slots; `Theme.kt:18-25` bridges 6 of Material's 15.
- The 10 unused imports are in `core/ui/composables/VibeMenu.kt` (lines 3, 6, 7, 8, 9, 10,
  13, 15, 18, 21), including two different `Icon` imports on 13 and 15 that only compile
  because neither is used. `MainActivity.kt` is clean.
- `res/values/colors.xml` holds 7 unused wizard template colors. `R.color` is referenced
  from zero places.

## Toolchain quirks — verify before "fixing"

- **AGP 9.4.1 modern DSL** in `app/build.gradle.kts`: `compileSdk { version = release(37) }` and
  `buildTypes.release { optimization { … } }`. Valid AGP 9. Do **not** rewrite to `compileSdk = 37`
  or `isMinifyEnabled`.
- **R8 is disabled** (`optimization { enable = false }`) and `app/src/main/keepRules/rules.keep` is
  entirely comments. Keep-rule work is inert until optimization is re-enabled.
- Gradle 9.6.0, Kotlin 2.2.10 (compose plugin) / 2.2.21 (serialization plugin), Compose BOM
  2026.02.01, navigation3 1.2.0. `minSdk` 24, `targetSdk` 37, `versionName "1.0.0"`,
  `versionCode 13`.
- `compileOptions` targets Java 11 while the daemon is **JDK 25** (pinned in
  `gradle/gradle-daemon-jvm.properties`, auto-provisioned via the foojay resolver in
  `settings.gradle.kts`) — intentional. There is no `kotlin { jvmToolchain() }` block.
- Configuration cache is on. If config looks stale, `./gradlew --stop` before suspecting your change.
- **`app/build/` survives branch switches** and is gitignored but not cleaned. It contains
  `.dex` files for deleted packages including a `core/ui/screen/SongCardData.dex` ghost.
  Run `./gradlew clean` before auditing anything that touches package structure, or you will
  chase symbols that only exist in stale build output.
- Release signing reads `MYAPP_RELEASE_*` from `~/.gradle/gradle.properties`, never the repo.
  `assembleDebug` ignores it.
- `local.properties` holds an absolute `sdk.dir`. Machine-local, gitignored, leave it alone.

## Commands

```bash
./gradlew :app:assembleDebug                      # the only real verification
./gradlew :app:installDebug                        # needs a device
./gradlew testDebugUnitTest                        # two generated stubs
./gradlew testDebugUnitTest --tests "dev.fiedri.vibe.SomeTest"   # single test
./gradlew connectedDebugAndroidTest                # needs a device
```

**No CI, no lint, no formatter, no detekt/ktlint.** `.github/` holds only issue templates and a
PR template — no workflows. The two test files are the generated `2 + 2` and package-name stubs.
Adding a lint/format config is welcome, but in its own commit.

## Do not ship this

`versionName = "1.0.0"` / `versionCode = 13` **must not reach a distribution channel.** The play
button toggles a boolean, every navigation callback in the detail screens is `{}`, the only
track is a hardcoded `Song("Sobreviviendo a la migración", …)` in `VibeApp.kt`, and the
manifest declares **zero permissions** — no MediaStore, no notifications. It also declares an
`audio/*` `VIEW` intent filter that nothing handles: no `getIntent()` read, no `onNewIntent`,
no service. The gate is behavioral, not a version string: see the checklist in `ROADMAP.md`
("Release gate").

## Bugs that are confirmed and not yet tracked anywhere

- Play/pause is inverted in the mini player: `Player.kt:274` picks `PlayArrow` while playing,
  `Player.kt:481` picks `Pause`. The expanded player is the correct one.
- `core/ui/composables/VibeToBar.kt:84` sets `contentDescription = "Buscar"` on the `MoreVert`
  button — the same string as the search icon at `:76`. TalkBack announces the overflow menu
  as "Search".
- `features/settings/presentation/settingsScreen.kt` hardcodes all of its labels:
  `"Settings"` at `:48`, `"Configuracion General"` three times (`:60`, `:75`, `:82`) and
  `contentDescription = "Volver atras"` at `:44`. All three are already translated in the
  three locales as `menus_mainmenu_settings`, `settings_general` and `menus_back`, so
  wiring them is mechanical. Its only interactive element is a `.clickable {}` with an
  empty body at `:72`.
- `MiniPlayer` declares `onPrevious` / `onNext` (`Player.kt:178-179`) and drops both —
  `Player.kt:254` and `:281` are `IconButton(onClick = { })`.
- The playlists empty state is unreachable: `playlists.kt:54` is `List(10)`, so the
  `if (playlists.isEmpty())` branch at `:78` is dead.
- The home sort menu renders and is wired to a callback, but `VibeTopBar` never passes
  `onSortSelected`, so choosing a sort field changes local state and nothing else.
