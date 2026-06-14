package net.sclan.minigames.ui

sealed class Screen {
    object Home : Screen()
    data class GameSetup(val game: GameId) : Screen()
    data class TicTacToe(val difficulty: TicTacToeDifficulty) : Screen()
    data class Game2048(val difficulty: TileMergeDifficulty) : Screen()
    data class Minesweeper(val difficulty: MinesweeperDifficulty) : Screen()
    object Settings : Screen()
}
