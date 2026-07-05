# Product Brief — Pocket Arcade Offline

## Overview

Pocket Arcade Offline is an Android app that bundles a growing collection of classic casual games playable entirely offline. No accounts, no location, no network required to play.

## Problem

Players often want a quick, low-friction game when offline (travel, poor signal, etc.). The category leaders (100M+ installs each) bundle heavy ad loads and declare collection/sharing of location, device IDs, and interaction data — with no data deletion.

## Solution

A single lightweight hub that launches immediately into a clean, searchable game shelf. Tap a game, read one screen of rules, pick a difficulty, play. Differentiators, per the July 2026 competitive research:

1. **Cleaner privacy posture** — no account, no location, local-only profile, in-app Delete Local Data.
2. **Better UX inside the catalog** — search, categories, favorites, recents.
3. **Fair monetization** — no ads during play; one-time Remove Ads Forever; reliable restore.
4. **Meaningful progression** — local XP/levels and per-game bests.
5. **Quality over quantity** — polished games with rules pagers and difficulty tiers.

## Target users

- Casual mobile gamers, all ages
- Anyone who frequently finds themselves without data (commuters, travellers)

## Current scope (v1.1 — Phase 1)

| Feature | Status |
|---------|--------|
| Hub with search, category chips, favorites, recents | ✅ |
| Game registry (category / players / session length) | ✅ |
| Tile Merge (1024/2048/4096 targets) | ✅ |
| Minesweeper (3 board sizes) | ✅ |
| Memory Match (3 grid sizes) | ✅ |
| Reaction Tap (3/5/10 rounds) | ✅ |
| Tic Tac Toe (2P / easy bot / smart bot) | ✅ |
| Rules pager + difficulty selector per game | ✅ |
| Local XP, levels, best scores, stats | ✅ |
| Remove Ads purchase + restore (Play Billing) | ✅ |
| Privacy screen + Delete Local Data | ✅ |
| Offline-first (no permissions needed) | ✅ |
| Material You theme (light + dark) | ✅ |

## Out of scope for this phase

- Ad SDK integration (ad slots reserved; Fair Ads Promise applies when added)
- Online multiplayer
- Cloud sync (would be opt-in only)
- Daily challenge, achievements (Phase 2)

## Success metrics

- Installs and D1 retention (target > 40 %)
- Crash-free sessions > 99 %
- APK size well under competitors' ~160 MB (target < 15 MB)

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- No external game libraries — game logic is pure Kotlin, unit-tested
- Google Play Billing for the single remove-ads product
- Target API 34, min API 24 (Android 7.0, ~96 % device coverage)
