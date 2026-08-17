#!/usr/bin/env bash
# Reproducible build for Vibe (Svelte + Capacitor + Android)
# Portable: all non-determinism derives from git, not local machine state.
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

# --- Deterministic seed from git (identical on every machine) ---
export SOURCE_DATE_EPOCH="$(git log -1 --pretty=%ct)"
export BUILD_ID="$(git rev-parse HEAD)"   # full commit hash — stable key for SvelteKit version.name

# --raw : skip APK normalization (e.g. if you sign the APK yourself afterwards)
RAW=0
for arg in "$@"; do
  case "$arg" in
    --raw) RAW=1 ;;
    --clean) ;; # kept for backward compat (gradle clean always runs)
    *) echo "Unknown arg: $arg"; exit 1 ;;
  esac
done

echo "=== Reproducible build ==="
echo "SOURCE_DATE_EPOCH=$SOURCE_DATE_EPOCH"
echo "BUILD_ID=$BUILD_ID"
echo

# 1. Clean web artefacts — guarantees no stale .svelte-kit/build/.vite caches
rm -rf build .svelte-kit .vite

# 2. Frontend (Vite/SvelteKit) — BUILD_ID propagated via env → version.json +
#    runtime id __sveltekit_<hash> are deterministic (git hash, not Date.now()).
BUILD_ID="$BUILD_ID" pnpm build

# 3. Sync web assets into the Android project
npx cap sync android

# 4. Android APK (release) — clean + --no-daemon for byte-exact packaging order
cd android
./gradlew clean assembleRelease --no-daemon

APK="app/build/outputs/apk/release/app-release.apk"
echo
echo "✓ Build complete: $APK"

# 5. Normalize ZIP timestamps (the last source of non-determinism).
#    AGP emits DOS "last modified" timestamps per zip-entry that vary between
#    builds even with identical content. We re-pack deterministically:
#    - SOURCE_DATE_EPOCH feeds every zip DOS timestamp
#    - `find | sort` fixes entry order
#    - `zip -X9` strips extra fields + uses fixed max-deflate
if [ "$RAW" -eq 0 ]; then
  echo "=== Normalizing APK timestamps ==="
  APK_ABS="$(realpath "$APK")"     # abs path — zip/zipalign cwd-independent
  NORM="${APK_ABS%.apk}-norm.apk"
  ALIGNED="${APK_ABS%.apk}-aligned.apk"

  find_zipalign() {
    command -v zipalign 2>/dev/null && return 0
    for d in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" "${ANDROID_SDK:-}" "${HOME}/Android/Sdk" "${HOME}/Library/Android/sdk"; do
      [ -n "$d" ] && for v in "$d"/build-tools/*/zipalign; do [ -x "$v" ] && { echo "$v"; return 0; }; done
    done
    return 1
  }
  ZA="$(find_zipalign || true)"

  TMPDIR_NORM="$(mktemp -d)"
  unzip -oq "$APK_ABS" -d "$TMPDIR_NORM"
  # normalize file mtimes too (belt-and-suspenders)
  ( cd "$TMPDIR_NORM" && find . -type f -exec touch -d "@${SOURCE_DATE_EPOCH}" {} + )
  rm -f "$NORM"
  # re-pack deterministic: abs output path, sorted order, no extra fields, fixed max-deflate
  ( cd "$TMPDIR_NORM" && find . -type f | LC_ALL=C sort | zip -X9 "$NORM" -@ >/dev/null )
  # realign 4-byte (APKs require it); deterministic when input order is fixed
  if [ -n "$ZA" ]; then
    "$ZA" -f 4 "$NORM" "$ALIGNED" 2>/dev/null
    mv -f "$ALIGNED" "$APK_ABS"
  else
    mv -f "$NORM" "$APK_ABS"
    echo "  (zipalign not found — APK is content-reproducible but unaligned)"
  fi
  rm -rf "$TMPDIR_NORM" "$NORM" "$ALIGNED"
  echo "✓ Normalized: $APK_ABS"
fi
