# Mini Game Hub

An offline-first casual mini-game collection for Android, built with Kotlin and Jetpack Compose.

## Games

| Game | Description |
|------|-------------|
| Tic Tac Toe | Classic 3×3 grid for two local players |
| 2048 | Slide tiles to combine them and reach 2048 |
| Minesweeper | 9×9 grid with 10 mines — tap to reveal, long-press to flag |

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with compile SDK 34

## Build locally

1. Clone the repository
2. Open the project in Android Studio — it will sync Gradle and download dependencies automatically
3. Run on a device or emulator with **Run ▶**

### Command-line build

If you have Gradle 8.4+ installed:

```bash
gradle wrapper --gradle-version 8.4
chmod +x gradlew
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

## Docs

- [Product Brief](docs/product-brief.md)
- [Roadmap](docs/roadmap.md)
