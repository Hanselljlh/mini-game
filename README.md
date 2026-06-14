# Mini Game Hub

An offline-first casual mini-game collection for Android, built with Kotlin and Jetpack Compose.

## Games

| Game | Description |
|------|-------------|
| Tic Tac Toe | Classic 3×3 grid with 2-player, easy bot, and smart bot modes |
| Tile Merge | Slide matching number tiles together until you reach the target tile |
| Minesweeper | Easy, Normal, and Hard boards — tap to reveal, long-press to flag |

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
./gradlew assembleDebug
```

The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

## Project layout

```
app/src/main/java/net/sclan/minigames/
├── MainActivity.kt           # Entry point, hosts navigation
└── ui/
    ├── Screen.kt             # Navigation sealed class
    ├── HomeScreen.kt         # Game hub / launcher
    ├── TicTacToeScreen.kt    # Tic Tac Toe
    ├── Game2048Screen.kt     # 2048
    ├── MinesweeperScreen.kt  # Minesweeper
    └── theme/                # Material 3 colour/type/theme
```

## CI

GitHub Actions builds a debug APK on every push and pull request to `main`.
See `.github/workflows/android-ci.yml`.

## Graphics and licenses

Current in-app graphics are original Compose/vector artwork created for this project. UI icons use Google's Material Icons, licensed under Apache 2.0. No random web/stock assets are bundled.

## Docs

- [Product Brief](docs/product-brief.md)
- [Roadmap](docs/roadmap.md)
