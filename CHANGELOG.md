# Changelog

All notable changes and new features of **Vibe** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) and this project adheres to [Semantic Versioning](https://semver.org/lang/es/).

---
## [unreleased]
## [0.6.1] - 2026-08-29
### Fixed
- Progress synchronization between the playback notification and the audio: precise seeking and correct position when pausing

## [0.6.0] - 2026-08-27
### Added
- Playback queue view
- Playback queue reordering
- Button to start shuffle playback from the Song view

## [0.5.3] - 2026-08-23
### Fixed
- Playlist restoration

## [0.5.2] - 2026-08-20

### Fixed
- [Error: You can't create another playlist named Favorites] when restoring playlists via JSON
- Visual bug in the mini player with infinite scrolling even when the text fit on screen

### Changed
- Removed permissions that were not necessary for the features

## [0.5.1] - 2026-08-17
### Added
- Song restarts when pressing previous after 3 seconds

## [0.5.0] - 2026-08-15 (beta)
> **Release note:** This release marks the transition from Alpha to Public Beta. The app already has the stable core feature set.

### Added
- Favorite songs management
- Deletion of duplicates in playlists
- Option to add a song as next in the queue
- Select all in multi-select
- Sorting options for songs, albums and artists

### Fixed
- Multi-select

### Changed
- Search now runs on a dedicated route.
- Migration of cache to filesystem

### Deleted
- Home view

## [0.4.0] - 2026-08-10

### Added
- Individual views for albums and artists.
### Changed
- Aesthetics of the individual playlist view and the player

## [0.3.0] - 2026-08-06

### Added
- Deletion of songs from local storage.
- Multi-select of songs.

# Fixed
- Repeat-one mode behavior: now it only repeats when the song finishes

# [0.2.4] - 2026-08-05
- Dialog to show errors to the user at runtime


## [0.2.3] - 2026-08-05

### Fixed
- Error when opening some playlists that contained the same song repeated (an error screen appeared when entering). Repeated songs are now filtered correctly and the playlist opens normally.
- Error when reopening the app with a song playing: the player state no longer fails when being restored.

## [0.2.2] - 2026-08-04

### Fixed
- Error opening Vibe after a **clean install** on some Android devices (error screen instead of the player). The cause was Android's auto-backup: when reinstalling, it restored the internal state of local storage in a broken condition. Backup is disabled to avoid this.

### Added
- **Playlist backup and restoration**: export your lists to a JSON (it's copied to the clipboard) and import them again later by pasting it. Useful when reinstalling the app or switching devices, since auto-backup has been disabled.

## [0.2.0] - 2026-08-04

### Added
- Option to update the media library manually.
- Per-song options submenu (for quick actions such as adding to playlists).
- Full support for playlists:
  - Add and remove songs.
  - Create and delete playlists.
  - Dedicated playback of playlists.

### Changed
- The playback queue now restricts navigation to the current context (playlist or global library).

## [0.1.2] - 2026-08-01

### Fixed
- `prev` / `next` navigation in shuffle mode ignored the real queue order.

## [0.1.1] - 2026-07-31

### Added
- Notification when pressing buttons whose features are not yet available.

## [0.1.0] - 2026-07-31

### Added
- Individual search filter for each section.
- Virtual rendering for long song lists and caching.
- Player with persistent state, controls (`play`, `pause`, `next`) and playback modes (`shuffle`, `repeat-one`, `repeat-all`).
- Main views: Home, Playlists, Albums, Songs and Artists.