# Roadmap

Strategy: build an offline-first "entertainment box" per the July 2026 competitive research —
quality over quantity, fair monetization, privacy as a differentiator. Phase numbers below map
to that plan (Phase 1 = shell + registry + 5 vertical-slice games).

## v1.1 — Phase 1: Shell & registry (current)

- [x] Home hub with search, category chips, favorites, and Continue Playing
- [x] Game registry (category, player count, session length metadata)
- [x] 5 vertical-slice games: Tile Merge, Minesweeper, Memory Match, Reaction Tap, Tic Tac Toe
- [x] Difficulty selection + rules pager per game
- [x] Local persistence: best scores, favorites, recents, XP/levels (SharedPreferences)
- [x] Privacy: Delete Local Data, no account, no analytics
- [x] Real Google Play Billing flow for Remove Ads + Restore Purchases
- [x] Unit tests run in CI

## v1.2 — Phase 2: MVP content (current)

- [x] Catalog grown to 12 games: added Snake, Four in a Row, Dots & Boxes, Word Search, Code Breaker, Mini Sudoku, Bubble Wrap
- [x] Two new categories: Words, Relax Toys
- [x] Daily offline challenge (deterministic date seed, +50 XP, no server)
- [x] 18 achievements computed from local stats
- [x] Every game reports completion events (XP + daily challenge credit)

## v1.3 — More content (current)

- [x] Catalog grown to 16 games: added Timing Stack, Maze Runner, Anagram Tiles, Mancala
- [ ] Grow catalog toward 25–30 games: Solitaire, Air-hockey duel, Word Ladder, Checkers, Simon Says, more relax toys
- [ ] Animated tile transitions in Tile Merge; win/loss celebration effects
- [ ] Migrate persistence to DataStore with schema versioning
- [ ] Ad wrapper with frequency caps (between sessions only, offline-safe)

## v1.4 — Accessibility & i18n

- [ ] Content descriptions for all interactive elements
- [ ] Colorblind palettes, reduced motion, large UI mode, haptics toggle
- [ ] Localisation: Spanish, French, German, Portuguese, Hindi, Indonesian, Arabic

## v2.0 — Growth

- [ ] Local pass-and-play tournament / family cup scoreboard
- [ ] Tablet / Chromebook layouts
- [ ] Themed content packs: Brain, Word, Duel, Relax
- [ ] Per-game automated smoke test harness (launch, pause/resume, airplane mode)
- [ ] Release build: minify, signing config, Play listing assets
