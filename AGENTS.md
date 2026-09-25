# AGENTS.md

Native Android music player ("Vibe Player"). **Migration in progress** from the Capacitor/SvelteKit app at
`~/Documentos/Programacion/projects/vibe-player` (source of truth for features, design, i18n, and DB schema — Drizzle/libSQL there maps to Room here). Single module, Jetpack Compose.

## Stack & layout

- Single Gradle module: `:app` (`app/`). Namespace `dev.fiedri.vibe`.
- Compose + Material3. Entry: `app/src/main/java/dev/fiedri/vibe/MainActivity.kt`.
- UI organized as `ui/theme`, `ui/screen/{songs,albums,artists,playlists}.kt`, `ui/components/` (all lowercase `.kt` filenames, existing convention).
- Room (`androidx.room:room-ktx`) is the declared DB dep. KSP is **not** yet configured — add the KSP plugin when Room entities land.
- No README, no lint/format config, only placeholder `ExampleUnitTest` — treat Gradle + source as truth.

## Toolchain quirks (verify before "fixing")

- **AGP 9.4.1** — modern DSL in `app/build.gradle.kts`: `compileSdk { version = release(37) }` and `buildTypes.release { optimization { enable = false } }`. These are valid AGP 9 syntax; do not "downgrade" to `compileSdk = 37` or `isMinifyEnabled`.
- Daemon JVM toolchain Java **25** (`gradle/gradle-daemon-jvm.properties`), auto-provisioned via foojay resolver in `settings.gradle.kts`. Java toolchain managed by Gradle, not the shell JDK.
- `compileOptions` targets Java 11 while daemon is 25 — intentional.
- minSdk 24, targetSdk 37. Configuration cache enabled.
- `local.properties` has absolute `sdk.dir` — machine-local, don't commit changes to it.

## Commands

```bash
./gradlew :app:assembleDebug        # build
./gradlew :app:installDebug          # install on connected device
./gradlew testDebugUnitTest          # unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (device required)
```

No CI, no lint/format checks wired — verification is the Gradle tasks above only.

## Conventions

- Commits: conventional commits, no AI attribution (see global config).
- When porting a feature from the old repo, check its `src/`, `messages/` (paraglide i18n) and `drizzle.config.ts` schema before inventing new behavior.
