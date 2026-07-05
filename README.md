# Pocket Arcade Offline

An offline-first casual mini-game hub for Android, built with Kotlin and Jetpack Compose.

**No account. No location. Built for offline play.** Every game works in airplane mode; scores, favorites, and XP live only on the device.

## Games

| Game | Category | Description |
|------|----------|-------------|
| Tile Merge | Brain & Logic | Slide matching number tiles together until you reach the target tile (1024/2048/4096) |
| Minesweeper | Brain & Logic | Easy, Normal, and Hard boards — tap to reveal, long-press to flag |
| Memory Match | Cards & Classics | Flip cards and find every matching pair in as few moves as you can |
| Reaction Tap | Arcade | Wait for green, then tap as fast as you can — average over 3/5/10 rounds |
| Tic Tac Toe | Local Duel | Classic 3×3 grid with 2-player, easy bot, and smart bot modes |

## App features

- **Hub shell** — search, category chips, Continue Playing, and Favorites sections
- **Game registry** — every game declares category, player count, and session length; new games plug into the shell
- **Local progression** — XP, levels, per-game best scores, games-played stats
- **Setup-first flow** — every game has a rules pager and difficulty selector before play
- **Privacy controls** — Delete Local Data button, no analytics, no account
- **Fair monetization** — one-time Remove Ads purchase (Google Play Billing) + Restore Purchases; no ads during play

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with compile SDK 34

## Build locally

1. Clone the repository
2. Open the project in Android Studio — it will sync Gradle and download dependencies automatically
3. Run on a device or emulator with **Run ▶**

### Command-line build

```bash
./gradlew test           # unit tests
./gradlew assembleDebug  # debug APK
```

The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

## Project layout

```
app/src/main/java/net/sclan/minigames/
├── MainActivity.kt            # Entry point, hosts navigation and wires repositories
├── billing/
│   ├── BillingRepository.kt   # Google Play Billing (remove-ads product)
│   └── PurchaseState.kt       # Purchase state model + testable holder
├── data/
│   └── ScoreRepository.kt     # Local scores, favorites, recents, XP (SharedPreferences)
└── ui/
    ├── Screen.kt              # Navigation sealed class
    ├── GameRegistry.kt        # Game metadata: category, players, session length
    ├── Difficulty.kt          # Game IDs and per-game difficulty enums
    ├── HomeScreen.kt          # Hub: search, categories, favorites, recents
    ├── GameSetupScreen.kt     # Rules pager + difficulty selector
    ├── SettingsScreen.kt      # Purchases, stats, privacy / delete data
    ├── TicTacToeScreen.kt     # Tic Tac Toe
    ├── Game2048Screen.kt      # Tile Merge (2048)
    ├── MinesweeperScreen.kt   # Minesweeper
    ├── MemoryMatchScreen.kt   # Memory Match
    ├── ReactionTapScreen.kt   # Reaction Tap
    └── theme/                 # Material 3 colour/type/theme
```

Game logic is deliberately kept in pure Kotlin functions (no Android/Compose dependencies) so it is unit-testable — see `app/src/test/`.

## CI

GitHub Actions runs the unit test suite and builds a debug APK on every push and pull request to `main`.
See `.github/workflows/android-ci.yml`.

## Graphics and licenses

Current in-app graphics are original Compose/vector artwork created for this project. UI icons use Google's Material Icons, licensed under Apache 2.0. Card faces use standard system emoji. No random web/stock assets are bundled.

## Docs

- [Product Brief](docs/product-brief.md)
- [Roadmap](docs/roadmap.md)
