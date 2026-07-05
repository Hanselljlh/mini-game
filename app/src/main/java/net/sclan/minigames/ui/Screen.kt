package net.sclan.minigames.ui

sealed class Screen {
    object Home : Screen()
    data class GameSetup(val game: GameId) : Screen()
    data class TicTacToe(val difficulty: TicTacToeDifficulty) : Screen()
    data class Game2048(val difficulty: TileMergeDifficulty) : Screen()
    data class Minesweeper(val difficulty: MinesweeperDifficulty) : Screen()
    data class MemoryMatch(val difficulty: MemoryMatchDifficulty) : Screen()
    data class ReactionTap(val mode: ReactionTapMode) : Screen()
    object Settings : Screen()
}
