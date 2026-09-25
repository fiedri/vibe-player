<!--
Thanks for opening an issue. If you are here because you want to contribute
code, this template is filled in for you with everything you need.

You are welcome to delete any part of this and just ask your question instead.
-->

## I want to contribute code

**Everything below is optional — delete whatever doesn't apply.**

### Have you picked something to work on?

The [Known issues table in the README](https://github.com/fiedri/vibe-player/blob/feat/migrate-to-native/README.md#known-issues)
is the best place to start. It is a list of real, confirmed bugs with `file:line`
locations. Comment on this issue with the one you want and we will confirm it is
unclaimed so two people don't do the same work.

**If you just want to be useful without picking anything specific**, these two are
about fifteen minutes each and need no knowledge of the codebase:

- [ ] **Delete the 7 unused template colors in `app/src/main/res/values/colors.xml`.**
      `purple_200`, `purple_500`, `purple_700`, `teal_200`, `teal_700`, `black` and
      `white` are left over from the Android Studio project wizard. Nothing in the
      app references them — the real palette lives in `ui/theme/Color.kt`.

- [ ] **Remove the unused imports in `app/src/main/java/dev/fiedri/vibe/MainActivity.kt`
      (lines 5-26).** They are leftovers from when the app shell lived in the
      Activity. Android Studio does it for you: right-click the file →
      **Optimize Imports**.

**If you want something with a bit more meat**, the play/pause icon in the mini
player is inverted — it shows `PlayArrow` while playing, while the expanded player
correctly shows `Pause` (`player/Player.kt:284` vs `:490`).

### Do you want a task picked for you?

Tell me what you're comfortable with and I'll find you one that fits:

- [ ] I have never contributed to an Android project
- [ ] I know Kotlin but not Jetpack Compose
- [ ] I know Compose — happy to take on a full screen
- [ ] I want to work on audio / ExoPlayer
- [ ] I want to work on the database / Room schema
- [ ] I'd rather translate the app to another language

Anything you'd enjoy, anything you want to learn, or "no idea, just point me at
something" all work.

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

Please open the PR against the `feat/migrate-to-native` branch, not `master`.
-->
