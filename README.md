# Pocket Arcade Offline

An offline-first casual mini-game hub for Android, built with Kotlin and Jetpack Compose.

**No account. No location. Built for offline play.** Every game works in airplane mode; scores, favorites, and XP live only on the device.

## Games (44)

| Game | Category | Description |
|------|----------|-------------|
| Tile Merge | Brain & Logic | Slide matching number tiles together until you reach the target tile (1024/2048/4096) |
| Minesweeper | Brain & Logic | Easy, Normal, and Hard boards — tap to reveal, long-press to flag |
| Code Breaker | Brain & Logic | Mastermind-style deduction — crack the 4-color code with feedback pegs |
| Mini Sudoku | Brain & Logic | Generated 4×4, 6×6, and 9×9 puzzles with conflict highlighting |
| Maze Runner | Brain & Logic | Freshly generated mazes — slide until you hit a wall, race to the exit |
| Simon Says | Brain & Logic | Repeat the flashing pad sequence as it grows, three speeds |
| Escape | Brain & Logic | Slide blockers along their tracks and free the red block — 6 handcrafted levels |
| Merge Chain | Brain & Logic | 2248-style chain merging with gravity refill |
| Cross Math | Brain & Logic | Interlocking equation puzzles with digit tiles |
| Number Connect | Brain & Logic | Retrace a hidden Hamiltonian path from 1 to N |
| Water Sort | Sort & Fill | Pour colored water between tubes until every tube is one color |
| Nuts & Bolts | Sort & Fill | Sort colored nuts onto matching bolts |
| Multi-Color Fill | Sort & Fill | Flood-fill the board into one color within the move limit |
| Color Blocks | Sort & Fill | Tap-match collapse with quadratic scoring and clear bonus |
| Block Fill | Sort & Fill | 1010-style piece placement with row/column clears |
| Sliding Puzzle | Sort & Fill | Classic 15-puzzle in 3×3 / 4×4 / 5×5 — always solvable shuffles |
| Maze Paint | Arcade | Slide-and-paint every square of a generated maze |
| Flappy Jump | Arcade | Tap-to-flap through scrolling pipe gaps |
| Penalty Kicks | Arcade | Timing-based shootout vs a diving keeper, 5 shots a round |
| Sand Fall | Relax Toys | Falling-sand physics toy with shifting rainbow colors |
| Fidget Spinner | Relax Toys | Flick-to-spin with momentum and a spin counter |
| Chalk Doodle | Relax Toys | Free drawing on a pocket chalkboard |
| Word Search | Words | Generated letter grids with 5–9 hidden words, all 8 directions |
| Anagram Tiles | Words | Unscramble letter tiles to rebuild hidden words over 5–7 rounds |
| Word Rescue | Words | Letter-guessing with a balloon-pop twist (6 misses allowed) |
| Word Guess | Words | 5-letter deduction with green/yellow/grey feedback over 6 tries |
| Word Ladder | Words | Transform one word into another one letter at a time (BFS-generated puzzles) |
| Memory Match | Cards & Classics | Flip cards and find every matching pair in as few moves as you can |
| Solitaire | Cards & Classics | Klondike draw-1 with tap-to-auto-move |
| War | Cards & Classics | Head-to-head card battles, aces high |
| Blackjack | Cards & Classics | Hit/stand vs a stand-on-17 dealer, no chips |
| Dominoes | Cards & Classics | Double-six block dominoes with boneyard vs bot |
| Reaction Tap | Arcade | Wait for green, then tap as fast as you can — average over 3/5/10 rounds |
| Snake | Arcade | Swipe-steered classic on a 15×15 grid with three speeds |
| Timing Stack | Arcade | Tap to drop sliding blocks — only the overlap survives, 12 layers to win |
| Tic Tac Toe | Local Duel | Classic 3×3 grid with 2-player, easy bot, and smart bot modes |
| Four in a Row | Local Duel | Connect-four duels vs a friend or a blocking/winning bot |
| Dots & Boxes | Local Duel | Pass-and-play line duel on 3×3 to 5×5 boards |
| Mancala | Local Duel | Classic Kalah rules — sow, capture, extra turns; 2-player or easy bot |
| Checkers | Local Duel | Mandatory-jump checkers with kings vs the bot |
| Ludo | Local Duel | Full classic board — vs bot, 2-player, or 4-player pass-and-play |
| Pong Duel | Local Duel | Paddle battle with spin — vs bot or 2 players on one screen |
| Air Hockey | Local Duel | Free-moving mallets, puck physics, goals — vs bot or 2 players |
| Bubble Wrap | Relax Toys | Pop every bubble. No score, no timer, pure calm |

## App features

- **Hub shell** — search, category chips, Continue Playing, and Favorites sections
- **Game registry** — every game declares category, player count, and session length; new games plug into the shell
- **Daily offline challenge** — deterministic date-seeded pick, +50 XP bonus, no server involved
- **Achievements** — 18 locally-computed achievements across all games
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
