# Product Brief — Mini Game Hub

## Overview

Mini Game Hub is an Android app that bundles a small collection of classic casual games playable entirely offline. No accounts, no ads, no network required.

## Problem

Players often want a quick, low-friction game when offline (travel, poor signal, etc.). Existing apps bundle ads or require logins even for simple games.

## Solution

A single lightweight app that launches immediately into a clean home screen. Tap a game, play, exit. No onboarding flow, no accounts.

## Target users

- Casual mobile gamers, all ages
- Anyone who frequently finds themselves without data (commuters, travellers)

## MVP scope

| Feature | Status |
|---------|--------|
| Home hub with game list | ✅ |
| Tic Tac Toe (local 2-player) | ✅ |
| 2048 (single player, swipe) | ✅ |
| Minesweeper (9×9 / 10 mines) | ✅ |
| Offline-first (no permissions needed) | ✅ |
| Material You theme (light + dark) | ✅ |

## Out of scope for MVP

- Online multiplayer
- Persistent high scores / leaderboard
- Sound effects / animations
- More than 3 games

## Success metrics

- Installs and D1 retention (target > 40 %)
- Crash-free sessions > 99 %
- APK size < 10 MB

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- No external game libraries — logic is pure Kotlin in the UI layer
- Target API 34, min API 24 (Android 7.0, ~96 % device coverage)
