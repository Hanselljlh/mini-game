package net.sclan.minigames.ui

sealed class Screen {
    object Home : Screen()
    object TicTacToe : Screen()
    object Game2048 : Screen()
    object Minesweeper : Screen()
}
