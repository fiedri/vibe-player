# Contributing to Vibe

First off, thank you for considering contributing! It's contributions like yours that make this project awesome.

---

## How Can I Contribute?

### Reporting Bugs
When creating a report, please include:
- A clear, descriptive title.
- Info about the device/OS you used (e.g., Android version, browser model).
- Steps to reproduce the issue.
- Expected vs actual behavior.
- Screenshots or screen recordings if applicable.

### Suggesting Enhancements
Feature requests are welcome! Please open an issue detailing the use case and why this feature would be useful.

---

## Local Development Setup

### 1. Prerequisites
- **Node.js**: v22+
- **PNPM**: (`npm install -g pnpm`)
- **Android Studio** and **Android SDK** (required for running on a physical device or emulator).

### 2. Clone and Install
```bash
git clone [https://github.com/fiedri/vibe-player.git](https://github.com/fiedri/vibe-player.git)
cd vibe-player
pnpm install

```

### 3. Run and Test

#### Web Mode (Browser)

For rapid UI/Svelte development:

```bash
pnpm dev

```

#### Live Reload on Android Device / Emulator

To test native Capacitor features (MediaSession, SQLite, etc.) on an Android device:

1. **Enable Developer Options & USB Debugging** on your Android phone.
2. Connect your phone to your computer via USB (make sure to accept the USB debugging prompt on your phone).
3. Run the development server with host access:
```bash
pnpm run dev --host

```


4. In a separate terminal, sync Capacitor and run on your Android device:
```bash
pnpm dev:android

```



#### Build and Run native APK directly

```bash
# Build the frontend assets and sync with Capacitor
pnpm build:android

# Compile and launch on connected Android device
npx cap run android

```

---

## Commit & PR Guidelines

* **Target Branch:** All Pull Requests should be created against the `dev` branch, **not** `main`.
* **Branch Naming:** Create a new feature/fix branch off `dev` before starting your work:
  ```bash
  git checkout dev
  git pull origin dev
  git checkout -b feature/your-feature-name # or fix/your-bug-fix
  ```

* Use [Conventional Commits](https://www.conventionalcommits.org/):
* `feat: ...` for new features.
* `fix: ...` for bug fixes.
* `docs: ...` for documentation changes.
* `chore: ...` for maintenance or config updates.


* Keep Pull Requests small and focused on a single responsibility.
