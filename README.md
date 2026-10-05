# CviTube ▶️

A responsive, private video and audio client built with Kotlin and Jetpack Compose.

## 🌟 Distinctive Identity
CviTube features an original visual language:
- **Palette**: Signature Teal (`#00B4D8`), Deep Indigo (`#6366F1`), and Warm Amber (`#F59E0B`).
- **Design System**: Rounded "card + chip" aesthetic with acrylic floating docks and minimal distraction.
- **Zero Ads, Zero Tracking, Zero Login**: 100% of user data (subscriptions, history, notes, queue, downloads) is stored locally on-device using Room Database with full JSON export/import.

## 🚀 Key Features

- **Mood Shelves**: Home screen organized into mood-based horizontal shelves (*Learn*, *Music*, *Chill*, *News*, *Focus*) rather than one endless addictive algorithm.
- **Notes & Chapters First**: Watch screen prioritizes timestamped personal notes, bookmarking, and chapter navigation over comments.
- **SponsorBlock Auto-Skip**: Auto-skips sponsor, intro, and outro segments via Piped API, toggleable per category.
- **Audio-Only Mode & Background Play**: Switch between video streaming and battery-saving audio-only mode with pulsating waveforms and MediaSession playback.
- **A-B Loop & Speed up to 4x**: Seamless loop between start and end timestamps, with precise playback speed controls.
- **Sleep Timer & Equalizer**: Fall asleep with automatic countdown timers and customize audio presets (*Bass Boost*, *Vocal Clarity*, *Acoustic*, *Lo-Fi*).
- **Focus Mode**: One-tap distraction-free mode hiding related videos and comments.
- **Daily Watch-Time Goal**: Track active watch time with gentle reminders for mindful viewing habits.
- **Offline Storage & Downloads**: Save video or audio tracks locally with real-time storage meter tracking.
- **Chronological Feed**: Pure chronological subscription updates with per-channel mute toggles.
- **Configurable Piped Instances**: Test and connect to public or self-hosted Piped instances.

## 📜 Attribution & License
CviTube is built on ideas and code from [LibreTube](https://github.com/libre-tube/LibreTube) (GPL-3.0) and uses public Piped APIs.
Licensed under the GNU General Public License v3.0 (GPL-3.0).
\n\n## Live data\nCviTube does not ship demo/fake video metadata, comments, statistics, or sample media files. Home, search, video details, comments, channels, and playback use live Piped API responses. If live providers are unavailable, the UI shows no results instead of inventing content.\n