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

## Your first contribution, in about fifteen minutes

You do not need to understand the whole app to make your first contribution. The task
we recommend is entirely mechanical, and you can see the result on screen by switching
your device language.

**Move the hardcoded strings into `strings.xml`.**

The app already ships translations in three locales — `res/values/` (English),
`res/values-es/` (Spanish) and `res/values-pl/` (Polish) — but the screens barely use
them. Around forty user-facing strings are written directly in the Kotlin source, so the
UI stays English no matter what language the phone is set to. Most of the translations
already exist and are simply wired to nothing.

Find them with:

```bash
rg 'contentDescription\s*=\s*"|text\s*=\s*"|Text\("' app/src/main/java/
```

Do **one screen per PR**. `settingsScreen.kt` is the easiest entry point — it hardcodes
all four of its labels, and all three keys it needs are already translated in all three
locales. A series of small PRs is easier to review than one huge one.

The pattern, established in `features/home/presentation/HomeContentMenu.kt`:

```kotlin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource

Text(stringResource(R.string.settings_general))

// for a count
Text(pluralStringResource(R.plurals.songs, size))
```

Grep `res/values/strings.xml` before adding a key — most already exist. If you do add
one, add it to all three locale files or the other two languages silently fall back to
English.

**The counts are a real bug, not cosmetics.** `detail.kt:121` builds its text by
concatenation — `if (size == 1) "1 cancion" else "$size canciones"` — which is
grammatically wrong in Polish, where 3 is "3 utwory" and 5 is "5 utworów". The app
already ships `<plurals name="songs">` and `<plurals name="albums">` with the correct
`one` / `few` / `many` / `other` forms for all three locales, and they are referenced
from **zero** places. `detail.kt:135` has the same problem. Fixing either one is a good
self-contained first PR on its own.

Two smaller tasks if you want something even quicker: **delete the 7 unused template
colors** in `app/src/main/res/values/colors.xml` (search the Kotlin sources for
`R.color` and you get zero hits), or **fix the overflow button's content description**
in `core/ui/composables/VibeToBar.kt:84`, which announces itself to TalkBack as "Search"
because it reuses the search icon's string. All of these are listed in the
[Known issues](README.md#known-issues) with `file:line` locations.

If you would rather be pointed at something, or you want to say what you want to
learn, open an issue using the **First-time contributor** template. It has a
checklist of areas — audio, search, Compose, translations, bug triage — and we will
find you something that fits.

**And if you are here to file a bug or request a feature**, use the Bug report or
Feature request templates instead.

## Table of contents

- [Where to send your pull request](#where-to-send-your-pull-request)
- [Your first contribution, in about fifteen minutes](#your-first-contribution-in-about-fifteen-minutes)
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
  styling. **Do not hardcode colors, font sizes or font weights in new code.** Use the
  tokens from the theme. It does have one known bug — it ignores light mode — listed
  in the [ROADMAP](ROADMAP.md).
- **Navigation is Navigation 3**, with a real back stack of six destinations. It is not
  a pager with an integer index. The `HorizontalPager` in `Home.kt` is only the tab
  switcher between the four library screens.
- Ten screens exist: the four library tabs, album / artist / playlist detail, search,
  settings, and the player in mini and expanded form. They are **still rendering
  placeholder data** built with `List(50) { … }` in eleven places.
- The player UI exists, including a working custom-drawn seek bar, but the audio
  engine is **not wired** — there is no Media3 or ExoPlayer behind the buttons.
- There is **no database, no ViewModels and no dependency injection.**
- Translations **do** exist — 156 strings in English, 155 in Spanish, 155 in Polish,
  ported from the web app's `messages/` catalogs. But only 24 of them are wired to
  `stringResource`, `R.plurals` is referenced from zero places, there are 26 hardcoded
  `contentDescription` literals, and no language switcher. So i18n is **partially
  adopted, not absent** — which makes it a good first contribution area.

### What is and is not on the classpath

This matters, because the version catalog and the build file disagree with the docs
that used to be here. As of now:

- **Wired and load-bearing:** `navigation3-ui` and `navigation3-runtime` (the
  `NavDisplay` and its `entryProvider`), `kotlinx-serialization-core` (the
  `@Serializable` annotations on the six `NavKey` types do nothing without it), and
  `material-icons-extended` (`SkipPrevious`, `Shuffle`, `Repeat`, `QueueMusic` and
  friends are extended-only).
- **Declared but not yet used:** `lifecycle-viewmodel-navigation3`,
  `adaptive-navigation3`, and three artifacts pinned outside the BOM
  (`ui-text`, `foundation-layout`).
- **Not in the project at all:** Room, Hilt, KSP, Media3 / ExoPlayer, Coil,
  DataStore, and `androidx.core:core-ktx`. There is no KSP block and no Hilt plugin in
  `app/build.gradle.kts`.

So if you were told that Navigation 3 or kotlinx-serialization were "declared in
anticipation", that was wrong — they are the app's navigation mechanism today. And if
you want to add Room, remember that `room-ktx`, `room-compiler` **and** the KSP plugin
must land in the same commit, or not one `@Entity` will compile.

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

`./gradlew :app:assembleDebug` is the only real verification in this repo. The Gradle
configuration cache is enabled, so if something looks like stale configuration, run
`./gradlew --stop` before suspecting your own change.

**This branch is a rewrite and it is moving fast.** Packages get moved and types get
renamed, and a restructure does not always update every consumer — that happened in
`a5aabbd`, which left three unresolved references until `b5cb490`. If you hit an
unresolved reference, run `./gradlew clean` and re-check the base branch before
assuming your change caused it: `app/build/` survives branch switches and can serve you
classes from a layout that no longer exists. If it is still broken after a clean, open
an issue rather than a PR — that is genuinely useful, not a wasted contribution.

**Expect this branch to be rough.** There is no CI, no lint setup, no formatter, and no
real test suite — the two test files are the generated `2 + 2` and package-name stubs.
If you add a formatter or a lint config as your first PR, that is genuinely welcome —
but keep it in its own commit so reviewers can ignore the noise.

## Signing

Release signing reads four `MYAPP_RELEASE_*` properties from your Gradle user home
(`~/.gradle/gradle.properties`), not from the repository, so nothing secret is ever
committed. If you are not doing a release build, you do not need to set them up —
`assembleDebug` ignores them.

## Project layout and conventions

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

- **File casing is not uniform, and that is deliberate at this point.** `songs.kt`,
  `albums.kt`, `artists.kt`, `playlists.kt` and `search.kt` are lowercase;
  `albumDetails.kt`, `artistDetails.kt`, `playlistDetails.kt` and `settingsScreen.kt`
  are camelCase; component files are mostly PascalCase, with `detail.kt`,
  `songCard.kt`, `utils.kt` and `cardDataModel.kt` lowercase in the same package.
  **Match the file you are editing.** A rename war here is not a contribution.
- **File names do not always match their symbols.** `GridCard.kt` holds `CardGrid`,
  `ThumbnailsCard.kt` holds `ThumbnailCard`, `VibeToBar.kt` holds `VibeTopBar` (typo
  included), `HomeContentMenu.kt` holds `HomeMenu` and `HomeOptionsMenu.kt` holds
  `SettingsDrawer`.
- **Styling comes from the theme** — `VibeTheme.colors` and `VibeTheme.typography`,
  both `staticCompositionLocalOf`. This is the rule new code must follow. Be aware the
  existing tree breaks it in roughly 35 places (`Color(0x…)`, raw `.sp`, hardcoded
  `Color.White`), so a file you touch may already be non-compliant.
- **Keep the layering direction.** `core` is shared infrastructure, features may
  import it, and it must not import features — the composition root may do both. Nothing
  enforces this because there is a single Gradle module, so the IDE will happily offer
  you the import and the build will pass. **Review is the enforcement mechanism.** The
  tree currently breaks this in `VibeApp.kt`, `navigation.kt` and `VibeToBar.kt`; do not
  add a fourth.
- **Grid screens mirror each other.** `artists.kt` and `albums.kt` are maintained as
  literal counterparts — same structure, same card contract, same grid config. If you
  change one, change the other in the same PR.
- **Keep composables small and give them real parameter types.** `PlayerUiState` exists
  for exactly this reason: `Player` used to take fifteen positional parameters, and
  that is the kind of signature that breaks the next time a field is added.

## Porting a screen from the web version

This is the most common contribution, and the web version is a complete working
reference. It lives on the `master` branch of this same repository. Note the route
groups — they are parenthesised, so **quote the path** or the shell will choke:

```bash
# in a second clone or worktree of master
git show 'master:src/routes/(app)/albums/+page.svelte'
git show 'master:src/routes/(standalone)/album/[name]/+page.svelte'
```

The main screens live under `src/routes/(app)/`, and the detail, search and settings
screens under `src/routes/(standalone)/`. Translated copy lives in
`messages/{en,es}.json` at the repo root of that branch, which is where
`res/values/strings.xml` and `res/values-es/strings.xml` were generated from.

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
