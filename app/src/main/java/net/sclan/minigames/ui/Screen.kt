package net.sclan.minigames.ui

sealed class Screen {
    object Home : Screen()
    data class GameSetup(val game: GameId) : Screen()
    data class TicTacToe(val difficulty: TicTacToeDifficulty) : Screen()
    data class Game2048(val difficulty: TileMergeDifficulty) : Screen()
    data class Minesweeper(val difficulty: MinesweeperDifficulty) : Screen()
    data class MemoryMatch(val difficulty: MemoryMatchDifficulty) : Screen()
    data class ReactionTap(val mode: ReactionTapMode) : Screen()
    data class Snake(val difficulty: SnakeDifficulty) : Screen()
    data class FourInARow(val mode: FourInARowMode) : Screen()
    data class DotsAndBoxes(val size: DotsAndBoxesSize) : Screen()
    data class WordSearch(val difficulty: WordSearchDifficulty) : Screen()
    data class CodeBreaker(val difficulty: CodeBreakerDifficulty) : Screen()
    data class Sudoku(val difficulty: SudokuDifficulty) : Screen()
    data class BubbleWrap(val size: BubbleWrapSize) : Screen()
    data class TimingStack(val speed: TimingStackSpeed) : Screen()
    data class MazeRunner(val size: MazeSize) : Screen()
    data class AnagramTiles(val length: AnagramLength) : Screen()
    data class Mancala(val mode: MancalaMode) : Screen()
    data class SimonSays(val speed: SimonSpeed) : Screen()
    object Settings : Screen()
}
