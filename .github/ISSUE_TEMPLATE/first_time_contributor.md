<!--
Thanks for opening an issue. If you are here because you want to contribute
code, this template is filled in for you with everything you need.

You are welcome to delete any part of this and just ask your question instead.
-->

## I want to contribute code

**Everything below is optional — delete whatever doesn't apply.**

### Heads up: this branch is moving fast

`feat/migrate-to-native` is a rewrite of a SvelteKit app into native Android. The
project is going through an active restructuring, so files move and types get
renamed. If you open a PR and hit an unresolved reference, run
`./gradlew clean` and re-check the base branch before assuming your change broke
it — and if it's still broken, open an issue instead of a PR. That's genuinely
helpful here, not a wasted contribution.

### Have you picked something to work on?

The [Known issues section in the README](https://github.com/fiedri/vibe-player/blob/feat/migrate-to-native/README.md#known-issues)
is the best place to start. It's a list of real, confirmed bugs with `file:line`
locations. Comment on this issue with the one you want and we will confirm it is
unclaimed so two people don't do the same work.

**If you just want to be useful without picking anything specific**, the task below is
the one we recommend. It is entirely mechanical, and you can see the result on screen by
switching your device language. Every path is relative to the repository root and every
line number is from `feat/migrate-to-native`.

- [ ] **Move the hardcoded strings into `strings.xml`**

      The app already ships translations in **three locales** — `res/values/`
      (English), `res/values-es/` (Spanish) and `res/values-pl/` (Polish) — but the
      screens barely use them. Around forty user-facing strings are written directly in
      the Kotlin source, so the UI stays English no matter what language the phone is
      set to.

      **Find them yourself:**

      ```bash
      rg 'contentDescription\s*=\s*"|text\s*=\s*"|Text\("' app/src/main/java/
      ```

      **Do one screen per PR.** `settingsScreen.kt` is the easiest starting point —
      it hardcodes all four of its labels. Do not try to sweep the whole app at once;
      a series of small PRs is easier to review than one huge one.

      You will find that **most of the translations already exist** and are simply
      wired to nothing. In `settingsScreen.kt`, all three keys are already translated in
      all three locales:

      | Instead of | Use |
      | --- | --- |
      | `"Settings"` | `R.string.menus_mainmenu_settings` |
      | `"Configuracion General"` | `R.string.settings_general` |
      | `contentDescription = "Volver atras"` | `R.string.menus_back` |

      So that whole screen needs no new copy and no XML edits. Other keys sitting unused
      include `play`, `shuffle`, `songs_options_share`, `songs_options_delete`,
      `playlist_title`, `playlist_favorites`, `playlist_no_playlist`,
      `playlist_add_new_playlist`, `playlist_backup_export` and `menus_song_options` —
      grep `res/values/strings.xml` before you add anything new.

      A few strings have no key at all — `"Cover"`, `"Skip to previous"`, `"Next"`,
      `"Collapse"`. If you add one, add it to **all three** locale files, or the app
      will silently fall back to English for the other two languages.

      In Compose, wrap the reference in `stringResource` from
      `androidx.compose.ui.res`:

      ```kotlin
      import androidx.compose.ui.res.stringResource

      Text(
          stringResource(R.string.settings_general).uppercase(),
          // ...
      )

      Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = stringResource(R.string.menus_song_options),
          // ...
      )
      ```

      `features/home/presentation/HomeContentMenu.kt` shows the established pattern.

      **The plural counts are a real bug, not cosmetics.** `detail.kt:121` builds its
      text by concatenation:

      ```kotlin
      text = (if (resources.songs.size == 1) "1 cancion" else "${resources.songs.size} canciones").uppercase()
      ```

      That is grammatically wrong in Polish, where 3 songs is "3 utwory" and 5 is
      "5 utworów" — the string depends on the number, not just the noun. The app already
      ships a `<plurals name="songs">` with the correct `one` / `few` / `many` / `other`
      forms for all three locales, and it is currently referenced from **zero** places.
      Use `pluralStringResource` instead:

      ```kotlin
      import androidx.compose.ui.res.pluralStringResource

      Text(
          pluralStringResource(R.plurals.songs, resources.songs.size).uppercase(),
          // ...
      )
      ```

      `detail.kt:135` has the same problem with `R.plurals.albums`. Fixing either one is
      a good, self-contained first PR on its own.

**Two smaller tasks, if you prefer something even quicker.** Each is about ten minutes:

- [ ] **Fix the overflow button's content description** in
      `app/src/main/java/dev/fiedri/vibe/core/ui/composables/VibeToBar.kt`.
      Line 84 is the `MoreVert` button, and it sets
      `contentDescription = "Buscar"` — the exact same string as the search icon on
      line 76. TalkBack therefore announces the overflow menu as "Search". There is no
      existing key for this one, so you will add it to all three locale files.

- [ ] **Delete the 7 unused template colors in `app/src/main/res/values/colors.xml`.**
      `purple_200`, `purple_500`, `purple_700`, `teal_200`, `teal_700`, `black` and
      `white` are left over from the Android Studio project wizard. Nothing in the
      app references them — search the Kotlin sources for `R.color` and you will get
      zero hits. The real palette lives in
      `app/src/main/java/dev/fiedri/vibe/core/ui/theme/Color.kt`.

**If you want something with a bit more meat**, the play/pause icon in the mini
player is inverted — it shows `PlayArrow` while playing, while the expanded player
correctly shows `Pause`. The two sites are in the same file:

- mini player: `app/src/main/java/dev/fiedri/vibe/features/player/presentation/Player.kt:274`
- expanded player: `app/src/main/java/dev/fiedri/vibe/features/player/presentation/Player.kt:481`

One of them has the condition backwards. The fix is a one-line change, but it is a
good first look at how the two player layouts are meant to behave.

### Do you want a task picked for you?

Tell me what you're comfortable with and I'll find you one that fits:

- [ ] I have never contributed to an Android project
- [ ] I know Kotlin but not Jetpack Compose
- [ ] I know Compose — happy to take on a full screen
- [ ] I want to work on audio / ExoPlayer
- [ ] I'd rather wire the search screen to real data
- [ ] I'd rather translate the app to another language
- [ ] I want to help triage and confirm bugs

The database is **not** in that list on purpose. Room needs `room-ktx`,
`room-compiler` and the KSP plugin to land together before a single `@Entity` can
compile, so it is a multi-hour task rather than a first one. If the schema
interests you, say so and we'll pair on it.

---

## What I need to know

**Your setup** (approximate is fine, this is just so we can help you):

- Android Studio version:
- Operating system:
- Have you cloned the repo and checked out `feat/migrate-to-native`?

**If you hit a build error, paste it here.** The most common one by far is a
missing `local.properties` — Android Studio creates it for you, but if you built
from the command line you may need:

```properties
sdk.dir=/path/to/your/Android/sdk
```

The rest of the toolchain is handled by Gradle. The daemon needs **JDK 25** and
will download it automatically through the foojay resolver, so there is nothing
to install for that.

**Anything else you'd like to ask?** Put it below.

---

<!--
For reference, the four commands you will need:

    ./gradlew :app:assembleDebug        # build
    ./gradlew :app:installDebug         # install on a connected device
    ./gradlew testDebugUnitTest         # unit tests
    ./gradlew connectedDebugAndroidTest # instrumented tests (needs a device)

If you see stale `app/build/` output and something that looks like a type which
no longer exists, run `./gradlew clean` first. The directory is gitignored but it
survives on disk across branch switches, and it can be very confusing.

Please open the PR against the `feat/migrate-to-native` branch, not `master`.
-->
