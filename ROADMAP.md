# ROADMAP

Native rewrite of Vibe in Kotlin + Jetpack Compose. Tracked on the
`feat/migrate-to-native` branch. The web version's roadmap continues to apply to
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
- [x] Theme: 11 color tokens for light and dark, Inter variable font at six weights,
      six custom typography slots
- [x] Navigation shell with bottom bar and animated tab indicator
- [x] Library screen layouts: songs, albums, artists, playlists
- [x] Player UI: mini and expanded, with a custom-drawn seek bar that handles real
      tap and drag gestures
- [x] Launcher icon, default album/artist artwork

## Bugs to fix

Small and self-contained. Good first contributions.

- [ ] **Play/pause icon inverted in the mini player** — `Player.kt` picks `PlayArrow`
      when playing, while the expanded player correctly picks `Pause`
- [ ] **The theme ignores light mode** — `Theme.kt` hardcodes the dark color set for
      `VibeTheme.colors` while `MaterialTheme.colorScheme` respects `darkTheme`, so
      system-light mode renders mismatched
- [ ] **Mini player previous/next callbacks are empty** — parameters are threaded in
      and dropped
- [ ] **Dead buttons** — both playlist FABs, top bar menu/search/overflow, player
      share/favorite/more all have `onClick = {}`
- [ ] **The playlists empty state is unreachable** — the placeholder list is never empty
- [ ] **22 unused imports in `MainActivity.kt`**
- [ ] **7 unused wizard template colors in `res/values/colors.xml`**
- [ ] **`keepRules/rules.keep` is entirely commented out**, and R8 is disabled for
      release builds

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
- [ ] Media3 / ExoPlayer engine behind `player/Player.kt`
- [ ] `MediaSessionService` + foreground service + media notification
- [ ] Playback queue, queue reordering, shuffle / repeat-one / repeat-all
- [ ] Restore playback state when the app is reopened
- [ ] Interrupt handling (calls, audio focus, headset unplug)

### Phase 3 — Feature parity with the web version
Use `master` as the behavioral reference. Screen by screen, in this order:

- [ ] Real data in the four library screens
- [ ] Search and per-section filtering
- [ ] Playlists: create, rename, delete, add/remove songs
- [ ] Favorites
- [ ] Sorting options for songs, albums and artists
- [ ] Multi-select and "play next"
- [ ] Share song, delete song
- [ ] Settings, including the language switcher

### Phase 4 — Polish
- [ ] Translations: `values/`, `values-es/` (the web version shipped `en` and `es`).
      Note that song counts are currently string-concatenated instead of using
      `<plurals>`, which is why English and Spanish are mixed in the UI today
- [ ] Shape tokens — there is no `Shape.kt` yet, so components hardcode
      `RectangleShape` where they want sharp corners
- [ ] Dynamic color, and a fix for the light-mode bug listed above
- [ ] Map all ~15 Material typography slots, not just the 6 currently bridged
- [ ] Accessibility pass: talkback labels, touch targets, contrast. Several
      `contentDescription` strings are hardcoded Spanish today
- [ ] Baseline profile

### Phase 5 — Release
- [ ] **Pass the release gate above** — this blocks every other item in this phase
- [ ] CI on every PR: `testDebugUnitTest` + `assembleDebug`
- [ ] Rewrite the release workflow for Gradle (the current one assumes pnpm and an
      `android/` subdirectory, so it does not apply here)
- [ ] Decide on the F-Droid / IzzyOnDroid recipe — the current `receta.yml` builds
      with pnpm and needs to be rewritten
- [ ] Reproducible build, which the web version cared about and should not lose
- [ ] Signing setup for release builds

## Carried over from the web version

- [ ] Equalizer
- [ ] Editing file metadata
