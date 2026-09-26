# AGENTS.md

Native Android music player ("Vibe Player"), Kotlin + Jetpack Compose. Branch
`feat/migrate-to-native`. Single Gradle module `:app`, namespace `dev.fiedri.vibe`.

It is a rewrite of a SvelteKit/Capacitor web app that lives in a **separate clone** at
`~/Documentos/Programacion/projects/vibe-player` (branches `master` / `dev`). That repo is
the behavioral reference — check its `src/`, `messages/` and `drizzle.config.ts` before
inventing behavior. Everything it lacks (data, playback, i18n) is absent here too, on purpose.

## Docs in this repo disagree with the code. Trust in this order

`README.md` and `CONTRIBUTING.md` are **stale — do not follow their layout or dependency
sections.** They still document `ui/VibeApp.kt`, `ui/screen/`, `player/Player.kt`, and claim
`room-ktx`, all three `navigation3` artifacts, `lifecycle-runtime-ktx` and
`kotlinx-serialization-core` are "declared but unused". All of those dependencies were
**deleted**; they are absent from `gradle/libs.versions.toml` and `app/build.gradle.kts`.
Their "known issues" lists have drifted too (the 22 unused `MainActivity.kt` imports are gone).

Trust, in order:

1. `app/build.gradle.kts` + `gradle/libs.versions.toml` + the source tree
2. `ARCHITECTURE.md` — current, and the authority on which layers exist and why
3. `ROADMAP.md` — ordered plan + the release gate
4. `README.md` / `CONTRIBUTING.md` — intent and style rules only; **verify their facts first**

## Actual layout

```
app/src/main/java/dev/fiedri/vibe/
├── MainActivity.kt                        # single activity: VibeTheme { VibeApp() }
├── core/ui/
│   ├── VibeApp.kt                         # composition root — MISPLACED, see below
│   ├── composables/                       # ThumbnailsCard.kt, VibeToBar.kt, utils.kt
│   ├── screen/                            # songs, albums, artists, playlists (.kt, lowercase)
│   └── theme/                             # Color.kt, Theme.kt, Type.kt — only 3 files
└── features/player/presentation/Player.kt
```

- `Pager` lives in `core/ui/composables/utils.kt:18`, not a `Pager.kt`. It is the only thing
  that knows about all four screens.
- `ThumbnailCard` is in `ThumbnailsCard.kt` — file name does not match the symbol.

## The dependency rule, and the two places it is already broken

> `core` is shared infrastructure. Features may import `core`. `core` must never import
> `features`. The composition root may import both.

Nothing enforces this: one Gradle module, so Kotlin packages are naming convention only. The
IDE will happily offer the import and the build will pass. **Review is the enforcement
mechanism.** Two live violations, both present today:

- `core/ui/VibeApp.kt:16-18` imports `features.player.presentation.{Player, PlayerState, Song}`.
  The composition root is *still under `core/`*, not at the app root. `ARCHITECTURE.md` claims
  this was fixed — it was not. Moving `VibeApp.kt` to `dev.fiedri.vibe/` is the fix, and it is
  the first thing to do before adding a second feature.
- `core/ui/composables/utils.kt:12-15` (`Pager`) imports all four screens, so the
  "shared by more than one feature and owned by none" package is not actually feature-agnostic.

## Do not build layers before their trigger

`ARCHITECTURE.md` defines an explicit trigger for each absent layer. The reflex to add a
Repository + ViewModel + Hilt is wrong here — each is deferred until it has something to do:

- **ViewModel + StateFlow** — first async data load (the first MediaStore query).
- **Repository interfaces** — a second implementation, or a test fake. One impl = not abstraction.
- **DI** — start now, but hand-written `AppContainer` (~40 lines). Hilt at ~10 bindings.
- **Room** — first `@Entity`. `room-ktx`, `room-compiler` **and** the KSP plugin must land
  together; `room-ktx` alone compiles no entity.
- **`:domain` JVM module** — when the domain model has real rules (favorites sentinel
  `"favoritos"`, queue order, playlist membership).
- **Navigation 3** — a real back stack. Today navigation is a `HorizontalPager` + integer index.

Cosmetic layering is called out as actively harmful: it makes a reader assume persistence,
threading and queue semantics are solved when they are the actual open risk.

## Conventions

- **Styling comes from `VibeTheme.colors` / `VibeTheme.typography`** (`staticCompositionLocalOf`).
  No `Color(0x…)` and no raw `.sp` outside `Color.kt` / `Type.kt`. This is the rule most often broken.
- Screens are **lowercase** (`songs.kt`). Shared composables and feature files are PascalCase
  (`VibeToBar.kt`, `Player.kt`), with `utils.kt` as the exception. The rule is not uniform —
  match the package you are in, and do not "fix" the casing.
- Styling token debt is real and known: no `Shape.kt`, no spacing tokens, so every `.dp` is a raw
  literal and components hardcode `RectangleShape`. `Player.kt` writes raw `.sp` and literal colors.
- `artists.kt` and `albums.kt` are maintained as literal counterparts — change one, change both.
- Tab titles and ~40 user-facing strings are hardcoded in composables, mixed Spanish and English.
  `strings.xml` has only `app_name`; `stringResource` is called from zero places.
- Commits: conventional commits, no AI attribution, no `Co-Authored-By`.

## Toolchain quirks — verify before "fixing"

- **AGP 9.4.1 modern DSL** in `app/build.gradle.kts`: `compileSdk { version = release(37) }` and
  `buildTypes.release { optimization { … } }`. Valid AGP 9. Do **not** rewrite to `compileSdk = 37`
  or `isMinifyEnabled`.
- **R8 is disabled** (`optimization { enable = false }`) and `app/src/main/keepRules/rules.keep` is
  entirely comments. Keep-rule work is inert until optimization is re-enabled.
- Gradle 9.6.0, Kotlin 2.2.10, Compose BOM 2026.02.01. `minSdk` 24, `targetSdk` 37.
  `compileOptions` targets Java 11 while the daemon is **JDK 25** (auto-provisioned via the foojay
  resolver in `settings.gradle.kts`) — intentional.
- Configuration cache is on. If config looks stale, `./gradlew --stop` before suspecting your change.
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
button toggles a boolean, every navigation callback is `{}`, the only track is a hardcoded
`Song("Sobreviviendo a la migración", …)` in `VibeApp.kt`, and the manifest declares **zero
permissions** — no MediaStore, no notifications. The gate is behavioral, not a version string:
see the checklist in `ROADMAP.md` ("Release gate").
