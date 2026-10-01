# AGENTS.md

Native Android music player ("Vibe Player"), Kotlin + Jetpack Compose.
Single Gradle module `:app`, namespace `dev.fiedri.vibe`.

## Branch reality — docs may be stale

Work happens on `main` (the default branch). The native rewrite merged into it;
`README.md`, `CONTRIBUTING.md` and `ROADMAP.md` may still name the old
`feat/migrate-to-native` branch — that branch no longer exists. The old
SvelteKit/Capacitor web app lives on `legacy` (frozen, behavioral reference
only); `master`/`dev` are gone. Trust `git branch`, not the prose. Same for any
doc claim about the tree: restructures move packages without touching docs.
After any move/rename, `:app:assembleDebug` is the checklist — e.g. `a5aabbd`
left three consumers broken until `b5cb490`.

The SvelteKit/Capacitor web app is the behavioral reference. It lives on
`legacy` of this same repo — verify before assuming a separate clone. Check its
routes/messages before inventing behavior.

## Commands

```bash
./gradlew :app:assembleDebug                      # the only real verification
./gradlew :app:installDebug                        # needs a device
./gradlew :app:testDebugUnitTest                   # unit tests (only generated stubs)
./gradlew :app:testDebugUnitTest --tests "dev.fiedri.vibe.SomeTest"  # single test
./gradlew connectedDebugAndroidTest                # needs a device
```

No CI, no lint, no formatter, no detekt/ktlint. `.github/` holds only issue/PR
templates — no workflows. The two unit tests are the generated `2 + 2` and
package-name stubs. Adding lint/format is welcome, in its own commit.

## Actual layout (verified)

```
app/src/main/java/dev/fiedri/vibe/
├── VibeApplication.kt              # @HiltAndroidApp
├── core/
│   ├── data/
│   │   ├── AudioStoreDataSource.kt # MediaStore query (IS_MUSIC, duration >= 30s, DATE_MODIFIED DESC)
│   │   ├── SongsRepository.kt      # @Singleton, StateFlow cache + forceRefresh
│   │   └── models/SongModel.kt
│   └── ui/
│       ├── MainActivity.kt         # @AndroidEntryPoint, enableEdgeToEdge, VibeTheme { VibeApp() }
│       ├── VibeApp.kt              # composition root — MISPLACED, see dependency rule
│       ├── NavigationKeys.kt       # 6 @Serializable NavKey types
│       ├── navigation.kt           # Navigator(backStack) + VibeNavGraph (NavDisplay)
│       ├── composables/            # GridCard→CardGrid, ThumbnailsCard→ThumbnailCard,
│       │                           # VibeMenu, detail.kt→DetailsScreen, songCard.kt→SongCard
│       │   └── models/             # CardData, SongCardUiState
│       └── theme/                  # Color.kt, Theme.kt, Type.kt
└── features/<name>/presentation/
    ├── home/         Home.kt, HomeTopBar.kt, HomeContentMenu.kt→HomeMenu,
    │                 HomeOptionsMenu.kt→SettingsDrawer, HomeScreenViewModel.kt
    ├── songs/        SongsScreen.kt, SongScreenViewModel.kt (+ SongOptionsSheet in file)
    ├── albums/       albums.kt, albumDetails.kt
    ├── artists/      artists.kt, artistDetails.kt
    ├── playlists/    playlists.kt, playlistDetails.kt
    ├── player/       Player.kt, PlayerMenu.kt, PlayerUiState.kt, PlayerViewModel.kt (untracked WIP)
    ├── search/       search.kt
    ├── settings/     settingsScreen.kt
    └── permissions/  permissionController.kt, permissionDialog.kt,
                      permissionBlockedScreen.kt, permissionRemember.kt
```

- There is **no `VibeToBar.kt`** anymore (deleted; top bar is now
  `features/home/presentation/HomeTopBar.kt`). Any doc referencing it is stale.
- File casing is not uniform: `songs`-era screens are lowercase (`songs.kt` is now
  `SongsScreen.kt` — PascalCase), older ones camelCase (`albumDetails.kt`,
  `settingsScreen.kt`), composables mixed (`detail.kt`, `songCard.kt`, `utils.kt`
  lowercase next to PascalCase). File name ≠ symbol (`GridCard.kt`→`CardGrid`,
  `ThumbnailsCard.kt`→`ThumbnailCard`, `HomeContentMenu.kt`→`HomeMenu`,
  `HomeOptionsMenu.kt`→`SettingsDrawer`). Match the file you edit; no rename wars.
- `artists.kt` and `albums.kt` are maintained as literal counterparts — change both
  in the same PR.

## What's on the classpath (docs are wrong — this is current)

Old docs say "no Hilt, no ViewModels, no DI, no MediaStore, zero permissions".
All four landed and were never documented:

- **Hilt wired**: `@HiltAndroidApp` (`VibeApplication`, declared in manifest),
  `@AndroidEntryPoint` (`MainActivity`), `@HiltViewModel` (`SongScreenViewModel`,
  consumed via `hiltViewModel()` in `SongsScreen.kt` and `HomeTopBar.kt`).
- **Data layer exists**: `AudioStoreDataSource` (MediaStore, `Dispatchers.IO`) →
  `SongsRepository` (`@Singleton`, `StateFlow` cache) → `SongScreenViewModel`
  (`Loading`/`Success`/`Error` sealed interface). No Room, no `SongDao`.
- **Permissions exist**: `features/permissions` (controller + dialog + blocked
  screen), manifest declares `READ_MEDIA_AUDIO` + `READ_EXTERNAL_STORAGE(maxSdk 32)`.
- **Room is half-declared**: `room-ktx` is in `app/build.gradle.kts` but
  `room-compiler` is absent and there is no KSP room processing — zero
  `@Entity`/`@Dao`/`@Database` can compile. Adding Room means compiler + KSP
  config in the same commit, not just the dependency.
- **Catalog trap**: `libs.versions.toml` `androidx-core-ktx` is actually
  `androidx.test:core-ktx`, not `androidx.core:core-ktx` — the name lies.
  `AudioStoreDataSource` imports `androidx.core.net.toUri`; if that import breaks,
  this mislabeling is why.
- **Still absent**: Media3/ExoPlayer (play button toggles a `Boolean`), Coil,
  DataStore. `lifecycle-viewmodel-navigation3` and `adaptive-navigation3` are
  declared but unused.

Navigation is Navigation 3 (`navigation3-ui`/`navigation3-runtime` 1.2.0) with a
hand-written `Navigator` behind `LocalNavigator`. `navigation3-*` and
`kotlinx-serialization-core` are load-bearing — the `@Serializable` nav keys do
nothing without the serialization plugin. The `HorizontalPager` in `Home.kt` is
only the tab switcher, not navigation.

## Dependency rule (unenforced — review is the mechanism)

> `core` is shared infra. Features may import `core`. `core` must never import
> `features`. The composition root may import both.

One Gradle module, so packages are convention only — the IDE offers the import
and the build passes. Live violations, all present:

- `core/ui/VibeApp.kt` + `core/ui/navigation.kt` import from `features.*` — the
  composition root is still under `core/`. Moving both to `dev.fiedri.vibe/` is
  the fix; do it before adding cross-feature wiring.
- `core/data/SongsRepository.kt:6` imports `features.player.presentation.Song`
  and never uses it — data layer depending on a UI type. Delete the import; the
  repository trades only in `SongModel`.

## Conventions

- Styling from `VibeTheme.colors` / `VibeTheme.typography`
  (`staticCompositionLocalOf`). No `Color(0x…)` / raw `.sp` outside `Color.kt` /
  `Type.kt`. The tree violates this in ~35 places — new code must still follow it.
- i18n: ~160 strings × 3 locales (`values/`, `values-es/`, `values-pl/`), mostly
  **unwired** — ~40 hardcoded literals across `Player.kt`, `playlists.kt`,
  `detail.kt`, `settingsScreen.kt`, `songCard.kt`, `ThumbnailsCard.kt`. Find them:
  `rg 'contentDescription\s*=\s*"|text\s*=\s*"|Text\("' app/src/main/java/`.
  Grep `strings.xml` before adding a key; add to all three locales or the others
  silently fall back to English. Pattern lives in `HomeContentMenu.kt`
  (`stringResource` / `pluralStringResource`).
- `R.plurals` is referenced from **zero** places; counts are string-concatenated
  (`detail.kt:122,136`) — grammatically wrong in Polish (`3 utwory` vs
  `5 utworów`; `values-pl` already ships the right forms). Real bug, good first PR.
- No `Shape.kt`, no spacing tokens — every `.dp` is a raw literal,
  `RectangleShape` hardcoded at ~8 sites.
- Commits: conventional commits, no AI attribution, no `Co-Authored-By`.

## Toolchain quirks — verify before "fixing"

- AGP 9.4.1 modern DSL: `compileSdk { version = release(37) }`,
  `buildTypes.release { optimization { … } }`. Valid — do not rewrite to
  `compileSdk = 37` / `isMinifyEnabled`.
- R8 disabled (`optimization { enable = false }`); `keepRules/rules.keep` is all
  comments. Keep-rule work is inert until re-enabled.
- Daemon is JDK 25, pinned in `gradle/gradle-daemon-jvm.properties`,
  auto-provisioned via foojay resolver in `settings.gradle.kts`. `compileOptions`
  targets Java 11 — intentional. No `kotlin { jvmToolchain() }` block.
- Configuration cache is on: `./gradlew --stop` before suspecting your change on
  stale config.
- `app/build/` survives branch switches (gitignored, not cleaned) and contains
  `.dex` ghosts of deleted packages. `./gradlew clean` before auditing package
  structure.
- Release signing reads `MYAPP_RELEASE_*` from `~/.gradle/gradle.properties`,
  never the repo. `assembleDebug` ignores it.
- `local.properties` (`sdk.dir`) is machine-local, gitignored, leave it alone.

## Do not ship this

`versionName 1.0.0` / `versionCode 13` must not reach a distribution channel.
Gate is behavioral (see `ROADMAP.md` "Release gate"): play toggles a boolean,
`VibeApp.kt` renders a hardcoded `Song("Sobreviviendo a la migración", …)`,
detail-screen callbacks are `{}`, no audio engine, no notification. The `audio/*`
`VIEW` intent filter in the manifest is handled by nothing (no `getIntent()`,
no service).

## Confirmed bugs, not yet tracked (all re-verified)

- Mini-player play/pause inverted: `Player.kt:274` shows `PlayArrow` while
  playing; `:478` (expanded) is correct. One-line fix, check both agree after.
- `HomeScreenViewModel` has `@Inject` constructor but **no `@HiltViewModel`**
  annotation, yet `HomeTopBar.kt:46` retrieves it via `hiltViewModel()` — crashes
  at runtime. Compare `SongScreenViewModel:25`, which is annotated.
- `Player.kt` declares `onPrevious`/`onNext` and drops both (`IconButton(onClick
  = { })`); same for playlist FABs and player share/favorite buttons.
- Playlists empty state unreachable (`List(10)` placeholder vs `isEmpty()` branch).
- Home sort menu is a no-op (local state only, `onSortSelected` never reaches data).
- Settings screen hardcodes all labels; only interactive element is `.clickable {}`
  with an empty body. Keys already exist in all three locales — mechanical fix.
- `VibeMenu.kt` still carries two competing `Icon` imports (material3 `:13` vs
  `SegmentedButtonDefaults.Icon` `:15`) plus unused imports — compiles only
  because neither is used.
- `res/values/colors.xml`: 7 unused wizard template colors (`R.color` has zero hits).
- Stray file: `app/src/androidTest/java/dev/fiedri/vibe/AudioStoreDataSource.kt`
  is untracked and sits outside any matching package dir — misplaced test scratch.
  `PlayerViewModel.kt` is also untracked WIP (plain `mutableStateOf`, no Hilt) —
  coordinate before duplicating player state.
