# ROADMAP

Native rewrite of Vibe in Kotlin + Jetpack Compose. Tracked on
`main` (the default branch). The web version's roadmap continues to apply to
anything not listed here.

## Release gate — 1.0.0 is not shippable yet

`versionName` is `1.0.0` (`versionCode = 13`) in `app/build.gradle.kts`, chosen to
mark the structural rewrite. **That number must not reach a distribution channel.**
The app currently cannot play audio: `onTogglePlay` flips a boolean, every
navigation callback is `{}`, and the only track is a hardcoded `Song(...)` in
`VibeApp.kt`. Publishing now would ship a music player that plays nothing.

A `versionName` is not observable by anyone until a build is actually distributed
— it appears in the F-Droid / IzzyOnDroid listing, the about screen, and the
install manager. Nobody sees `1.0.0` while it exists only in the build file, so
keeping it costs nothing and marks the rewrite now. The gate is on *distribution*,
not on the version string.

**1.0.0 may be submitted to F-Droid, IzzyOnDroid, or any other channel only when
every one of the following is observably true on a physical device.**

- [ ] Tapping a row in the Songs list produces audible sound, and the displayed
      position advances in step with what you can hear
- [ ] Play/pause results in audible playback or actual silence — not just a
      toggling icon. The mini player and the expanded player control the same
      playback, and both agree with each other
- [ ] Next and previous move to the adjacent real track and keep playing
- [ ] The seek bar actually seeks — dragging to the midpoint resumes the audio
      from the midpoint
- [ ] Playback continues after leaving the screen: backgrounding the app,
      navigating to another tab, and locking the device all keep the sound going
- [ ] A media notification is present, and its play/pause button controls
      playback from the lock screen
- [ ] All of the above runs against the real device library, with the hardcoded
      `Song("Sobreviviendo a la migración", ...)` placeholder deleted

Each item is a thing a person can observe, not a matter of opinion. If you cannot
tick one, the gate holds. Do not publish a build that fails any of them, and do
not treat "the UI is finished" as a substitute — the screens being complete is
what makes this gate easy to talk yourself out of.

## Done

- [x] Android project bootstrapped: AGP 9.4.1, Gradle 9.6, Kotlin 2.2.10, Compose BOM
- [x] Theme: 15 color tokens for light and dark, Inter variable font at six weights,
      six custom typography slots
- [x] Navigation on Navigation 3: six `@Serializable` nav keys, a real back stack
      behind `Navigator`, and a `NavDisplay` with an `entryProvider`
- [x] Top bar with an animated tab indicator that measures real tab positions
- [x] Library screen layouts: songs, albums, artists, playlists
- [x] Album, artist and playlist detail screens, sharing a reusable `DetailsScreen`
- [x] Search screen with per-section filtering and a debounced query
- [x] Settings screen (a stub)
- [x] Player UI: mini and expanded, with a custom-drawn seek bar that handles real
      tap and drag gestures
- [x] Context menus: song options bottom sheet, player dropdown, home overflow menu
      with per-tab sorting, and a slide-in settings drawer
- [x] Translations: `values/`, `values-es/` **and `values-pl/`**, ported from the web
      app's `messages/{en,es}.json` plus a community Polish catalog
- [x] Launcher icon, default album/artist artwork

## Next: make it real

Everything below Phase 0 is placeholder data. This is the whole job right now.

### Phase 1 — Data layer
- [ ] Read the device library from `MediaStore` (audio only, with permission handling)
- [ ] Add Room properly: the `room-ktx` dependency, the `room-compiler`
      annotation processor, **and** the KSP plugin, all three at once. The
      `room-ktx` declaration was removed as unused, so today there is no Room at
      all and no `@Entity`, `@Dao` or `@Database` can compile
- [ ] Design the Room schema. Note the web version stored favorites as a playlist
      literally named `"favoritos"`; decide deliberately whether Room gets a
      `kind`/`is_favorites` column or keeps the sentinel name, and write down the answer
- [ ] Port the `playlists` / `playlists_songs` tables (mind the `song_id` as text,
      and the missing unique constraint that made duplicate cleanup necessary)
- [ ] ViewModels + `StateFlow`; move all state out of the composables
- [ ] Dependency injection — pick a strategy and apply it consistently
- [ ] Artwork loading off the main thread, with the default-cover fallback

### Phase 2 — Audio
- [ ] Media3 / ExoPlayer engine behind `features/player/presentation/Player.kt`
- [ ] `MediaSessionService` + foreground service + media notification
- [ ] Playback queue, queue reordering, shuffle / repeat-one / repeat-all
- [ ] Restore playback state when the app is reopened
- [ ] Interrupt handling (calls, audio focus, headset unplug)

### Phase 3 — Feature parity with the web version
Use `legacy` as the behavioral reference. Screen by screen, in this order:

- [ ] Real data in the four library screens
- [ ] Build the search index and wire the existing search screen to it — the screen
      and its per-section filtering are already built, filtering a hardcoded `List(50)`
- [ ] Playlists: create, rename, delete, add/remove songs — the list UI and two dead
      FABs exist
- [ ] Favorites — `playlists.kt:52` currently hardcodes a `"favoritos"` sentinel row
- [ ] Make the existing sort menu reach the data layer — `onSortSelected` is never
      passed by `VibeTopBar`
- [ ] Multi-select and "play next"
- [ ] Share song, delete song — the menu items render, the handlers are empty
- [ ] Build out the settings screen and add the language switcher. The three locales
      already exist; only 24 of 156 strings are wired to `stringResource`

### Phase 4 — Polish
- [ ] **Move the ~40 hardcoded strings into `strings.xml`** — mostly `Player.kt`,
      `playlists.kt`, `detail.kt` and `settingsScreen.kt`. Most keys already exist
      translated in three locales and are wired to nothing. Best done as one PR per
      screen
- [ ] **Use `R.plurals` for the counts** — it is referenced from zero places, and
      `detail.kt:121`/`:135` plus `playlists.kt:169`/`:216` string-concatenate instead.
      The current output is grammatically wrong in Polish, where `values-pl` already
      has the correct `one`/`few`/`many`/`other` forms
- [ ] Wire the remaining 132 translated strings that are defined but unreferenced
- [ ] Document where `values-pl/strings.xml` came from — its two siblings carry
      "Auto-generated from messages/{en,es}.json" headers and `messages/` on `legacy`
      only holds `en` and `es`
- [ ] Replace the ~35 hardcoded colors and font sizes with theme tokens — the rule
      already exists, the tree just does not follow it
- [ ] Shape tokens — there is no `Shape.kt` yet, so components hardcode
      `RectangleShape` where they want sharp corners
- [ ] Dynamic color, and a fix for the light-mode bug listed above
- [ ] Map all ~15 Material typography slots, not just the 6 currently bridged
- [ ] Accessibility pass: talkback labels, touch targets, contrast. There are 26
      hardcoded `contentDescription` literals, 9 of them Spanish
- [ ] Move the composition root out of `core/` — `VibeApp.kt` and `navigation.kt` both
      import from `features`, which inverts the layering rule
- [ ] Baseline profile

### Phase 5 — Release
- [ ] **Pass the release gate above** — this blocks every other item in this phase
- [ ] CI on every PR: `testDebugUnitTest` + `assembleDebug`
- [ ] A release workflow for Gradle. There is none in this repo yet — `.github/`
      holds only issue templates and a PR template, no workflows
- [ ] Decide on the F-Droid / IzzyOnDroid recipe. The web version's `receta.yml` lives
      on `legacy` and builds with pnpm, so it needs to be written from scratch here
- [ ] Reproducible build, which the web version cared about and should not lose
- [ ] Re-enable R8 and write real keep rules — the signing config is already wired to
      the `MYAPP_RELEASE_*` properties, but `optimization { enable = false }` means
      keep-rule work is currently inert

## Carried over from the web version

- [ ] Equalizer
- [ ] Editing file metadata
