# Contributing to Vibe

First off, thank you for considering contributing!

> **Read this first.** Vibe is being rewritten from a Svelte/Capacitor web app into
> a native Android app written in Kotlin with Jetpack Compose. That rewrite lives in
> the **`feat/migrate-to-native`** branch, and **that is the branch every pull request
> should target.** The `master` and `dev` branches hold the old web version and are
> kept as a reference for what the app is supposed to do — they are not accepting new
> features.
>
> If you want to help, you are in the right place. Most of the app is not written yet.

## Table of contents

- [Where to send your pull request](#where-to-send-your-pull-request)
- [What the project looks like today](#what-the-project-looks-like-today)
- [Local development setup](#local-development-setup)
- [Build and test commands](#build-and-test-commands)
- [Project layout and conventions](#project-layout-and-conventions)
- [Porting a screen from the web version](#porting-a-screen-from-the-web-version)
- [Commit guidelines](#commit-guidelines)
- [Reporting bugs](#reporting-bugs)

## Where to send your pull request

- **Base branch:** `feat/migrate-to-native`, not `master` and not `dev`.
- **Branch naming:** branch off `feat/migrate-to-native` before you start:
  ```bash
  git fetch origin
  git checkout feat/migrate-to-native
  git pull
  git checkout -b feat/my-feature   # or fix/, chore/, docs/
  ```
- Keep pull requests small and focused on a single responsibility. A screen port and
  an unrelated refactor in the same PR is a PR we cannot review properly.

## What the project looks like today

Being honest about this, because it saves you from a wasted afternoon:

- The theme layer (colors and typography) is done and is the source of truth for all
  styling. **Do not hardcode colors, font sizes or font weights in screens.** Use the
  tokens from the theme. It does have one known bug — it ignores light mode — listed
  in the [ROADMAP](ROADMAP.md).
- The navigation shell and the four library screen layouts (songs, albums, artists,
  playlists) exist, but they are **still rendering placeholder data** built with
  `List(50) { … }`.
- The player UI exists, including a working custom-drawn seek bar, but the audio
  engine is **not wired** — there is no Media3 or ExoPlayer behind the buttons.
- There is **no database, no ViewModels, no dependency injection and no i18n yet.**

**Careful with the dependency list.** Several libraries are already on the classpath
but completely unused: `room-ktx` (inert — the KSP plugin is not configured, so no
`@Entity` can compile), all three `navigation3` artifacts, `lifecycle-runtime-ktx`,
`kotlinx-serialization-core`, `remote-creation-core`, and `material-icons-extended`.
Do not assume Navigation 3 or Room are set up because you can see them in
`libs.versions.toml`. They are declared in anticipation, not wired.

So the highest-value contributions right now are: wiring the audio engine, reading the
device library, and replacing placeholder data with real state. See the
[ROADMAP](ROADMAP.md) for the full picture, and the
[Known issues](README.md#known-issues) for self-contained bug fixes.

## Local development setup

### 1. Prerequisites

- **Android Studio** (recent enough to support AGP 9).
- **JDK 25** — the Gradle daemon runs on Java 25. The repository pins this in
  `gradle/gradle-daemon-jvm.properties`, and Gradle will **auto-provision it** for you
  through the foojay resolver declared in `settings.gradle.kts`. You do not need to
  install it by hand, but your shell JDK should be a version Gradle accepts.
- The **Android SDK** with the platform matching `compileSdk`.

There is no other toolchain. No Node, no pnpm, no Capacitor — the web toolchain is
gone.

### 2. Clone

```bash
git clone https://github.com/fiedri/vibe-player.git
cd vibe-player
git checkout feat/migrate-to-native
```

`local.properties` is machine-local and not committed. It only needs your SDK path:

```properties
sdk.dir=/path/to/your/Android/sdk
```

If you opened the project in Android Studio, it generates this file for you.

### 3. Build

```bash
./gradlew :app:assembleDebug
```

## Build and test commands

```bash
./gradlew :app:assembleDebug          # build the debug APK
./gradlew :app:installDebug            # install on a connected device or emulator
./gradlew testDebugUnitTest            # unit tests
./gradlew connectedDebugAndroidTest    # instrumented tests (needs a device)
```

The Gradle configuration cache is enabled. If you hit something that looks like stale
configuration, `./gradlew --stop` before suspecting your own change.

**Expect this branch to be rough.** There is no lint setup, no formatter, and no real
test suite — the two test files are the generated `2 + 2` and package-name stubs. If
you add a formatter or a lint config as your first PR, that is genuinely welcome — but
keep it in its own commit so reviewers can ignore the noise.

## Signing

Release signing reads four `MYAPP_RELEASE_*` properties from your Gradle user home
(`~/.gradle/gradle.properties`), not from the repository, so nothing secret is ever
committed. If you are not doing a release build, you do not need to set them up —
`assembleDebug` ignores them.

## Project layout and conventions

```
app/src/main/java/dev/fiedri/vibe/
├── MainActivity.kt          # single activity, Compose entry point
├── player/Player.kt         # audio engine
└── ui/
    ├── VibeApp.kt           # root composable
    ├── components/          # reusable composables
    ├── screen/              # one file per screen
    └── theme/               # colors, typography, shapes
```

- **Screen files are lowercase** (`songs.kt`, `playlists.kt`), and so are component
  files. This is deliberate. Do not "fix" it to PascalCase.
- **Styling comes from the theme.** Colors, type and shape must be referenced from the
  theme tokens. This is the rule most often broken by new contributors, and the reason
  the UI drifted apart before.
- **Grid screens mirror each other.** `artists.kt` and `albums.kt` are maintained as
  literal counterparts — same structure, same card contract, same grid config. If you
  change one, change the other in the same PR.
- Keep composables small and give them real parameter types. A composable that takes
  fifty positional parameters is a composable that needs a data class.

## Porting a screen from the web version

This is the most common contribution, and the web version is a complete working
reference. Check the `master` branch:

```bash
# in a second clone or worktree of master
git show master:src/routes/albums/+page.svelte
```

When you port one:

1. Match the **behavior** first, then the visuals. Behavior is what people notice.
2. Pull colors, spacing and type from the theme tokens. Do not copy hex values.
3. Include a screenshot in the PR. Visual changes are reviewed visually.
4. Say in the PR description which web file you ported from, so a reviewer can diff
   them side by side.

## Commit guidelines

Use [Conventional Commits](https://www.conventionalcommits.org/):

- `feat: ...` — new functionality
- `fix: ...` — bug fixes
- `docs: ...` — documentation
- `chore: ...` — maintenance or config
- `refactor: ...` — changes that neither fix a bug nor add a feature
- `perf: ...` — performance improvements

Keep the subject short and in the imperative. No AI attribution and no `Co-Authored-By`
trailers.

## Reporting bugs

Open an issue using the **Bug report** template. It will ask for the device model, the
Android version, the API level, the app version and the logcat output. All of that is
genuinely needed — a "it crashes" report with no API level is close to unreproducible.

For a crash, this gets you the right logs:

```bash
adb logcat --pid=$(adb shell pidof dev.fiedri.vibe)
```

## Code of Conduct

Everyone participating is expected to follow the
[Code of Conduct](CODE_OF_CONDUCT.md).
