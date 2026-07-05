package net.sclan.minigames.ui

enum class GameId(val title: String, val shortDescription: String) {
    TileMerge("Tile Merge", "Slide matching numbers together until you reach the target tile."),
    Minesweeper("Minesweeper", "Reveal safe squares, flag mines, and clear the board."),
    TicTacToe("Tic Tac Toe", "Line up three marks before your opponent does."),
    MemoryMatch("Memory Match", "Flip cards and find every matching pair in as few moves as you can."),
    ReactionTap("Reaction Tap", "Wait for green, then tap as fast as you can."),
    Snake("Snake", "Steer the snake to the food and grow without hitting walls or yourself."),
    FourInARow("Four in a Row", "Drop discs and connect four before your opponent."),
    DotsAndBoxes("Dots & Boxes", "Draw lines to close boxes — most boxes wins."),
    WordSearch("Word Search", "Find every hidden word in the letter grid."),
    CodeBreaker("Code Breaker", "Crack the secret color code using logic and feedback pegs."),
    Sudoku("Mini Sudoku", "Fill the grid so every row, column, and box has each number once."),
    BubbleWrap("Bubble Wrap", "Pop every bubble on the sheet. Endlessly satisfying."),
    TimingStack("Timing Stack", "Tap at the right moment to stack blocks sky-high."),
    MazeRunner("Maze Runner", "Slide through the maze and find the exit as fast as you can."),
    AnagramTiles("Anagram Tiles", "Unscramble the letters to rebuild the hidden word."),
    Mancala("Mancala", "Sow seeds around the board and capture more than your opponent."),
    SimonSays("Simon Says", "Watch the pads light up, then repeat the growing sequence.")
}

enum class TileMergeDifficulty(val label: String, val targetTile: Int, val startTiles: Int) {
    Easy("Easy", 1024, 4),
    Normal("Normal", 2048, 2),
    Hard("Hard", 4096, 2)
}

data class MinesweeperConfig(val rows: Int, val cols: Int, val mines: Int)

enum class MinesweeperDifficulty(val label: String, val config: MinesweeperConfig) {
    Easy("Easy", MinesweeperConfig(8, 8, 8)),
    Normal("Normal", MinesweeperConfig(9, 9, 10)),
    Hard("Hard", MinesweeperConfig(12, 12, 30))
}

enum class TicTacToeDifficulty(val label: String) {
    TwoPlayer("2 Players"),
    EasyBot("Easy Bot"),
    SmartBot("Smart Bot")
}

enum class MemoryMatchDifficulty(val label: String, val rows: Int, val cols: Int) {
    Easy("Easy", 3, 4),
    Normal("Normal", 4, 4),
    Hard("Hard", 6, 4);

    val pairCount: Int get() = rows * cols / 2
}

enum class ReactionTapMode(val label: String, val rounds: Int) {
    Quick("Quick • 3 rounds", 3),
    Standard("Standard • 5 rounds", 5),
    Marathon("Marathon • 10 rounds", 10)
}

enum class SnakeDifficulty(val label: String, val tickMs: Long) {
    Slow("Relaxed", 280L),
    Normal("Normal", 190L),
    Fast("Fast", 120L)
}

enum class FourInARowMode(val label: String) {
    TwoPlayer("2 Players"),
    EasyBot("Easy Bot"),
    SmartBot("Smart Bot")
}

enum class DotsAndBoxesSize(val label: String, val boxes: Int) {
    Small("Small 3×3", 3),
    Medium("Medium 4×4", 4),
    Large("Large 5×5", 5)
}

enum class WordSearchDifficulty(val label: String, val gridSize: Int, val wordCount: Int) {
    Easy("Easy 8×8", 8, 5),
    Normal("Normal 10×10", 10, 7),
    Hard("Hard 12×12", 12, 9)
}

enum class CodeBreakerDifficulty(val label: String, val colors: Int, val maxGuesses: Int) {
    Easy("Easy • 6 colors", 6, 12),
    Normal("Normal • 7 colors", 7, 10),
    Hard("Hard • 8 colors", 8, 10)
}

enum class SudokuDifficulty(val label: String, val size: Int, val boxRows: Int, val boxCols: Int, val givens: Int) {
    Mini("Mini 4×4", 4, 2, 2, 8),
    Midi("Midi 6×6", 6, 2, 3, 16),
    Classic("Classic 9×9", 9, 3, 3, 34)
}

enum class BubbleWrapSize(val label: String, val rows: Int, val cols: Int) {
    Pocket("Pocket sheet", 8, 6),
    Standard("Standard sheet", 10, 7),
    Jumbo("Jumbo sheet", 12, 8)
}

enum class TimingStackSpeed(val label: String, val tickMs: Long) {
    Chill("Chill", 150L),
    Normal("Normal", 105L),
    Turbo("Turbo", 70L)
}

enum class MazeSize(val label: String, val size: Int) {
    Small("Small 8×8", 8),
    Medium("Medium 11×11", 11),
    Large("Large 14×14", 14)
}

enum class AnagramLength(val label: String, val rounds: Int, val minLen: Int, val maxLen: Int) {
    Short("Short words • 5 rounds", 5, 4, 5),
    Mixed("Mixed words • 7 rounds", 7, 4, 7),
    Long("Long words • 7 rounds", 7, 6, 8)
}

enum class MancalaMode(val label: String) {
    TwoPlayer("2 Players"),
    EasyBot("Easy Bot")
}

enum class SimonSpeed(val label: String, val showMs: Long, val gapMs: Long) {
    Relaxed("Relaxed", 600L, 250L),
    Normal("Normal", 430L, 180L),
    Fast("Fast", 280L, 120L)
}

data class GameSetupChoice(
    val tileMerge: TileMergeDifficulty = TileMergeDifficulty.Normal,
    val minesweeper: MinesweeperDifficulty = MinesweeperDifficulty.Normal,
    val ticTacToe: TicTacToeDifficulty = TicTacToeDifficulty.TwoPlayer,
    val memoryMatch: MemoryMatchDifficulty = MemoryMatchDifficulty.Normal,
    val reactionTap: ReactionTapMode = ReactionTapMode.Standard,
    val snake: SnakeDifficulty = SnakeDifficulty.Normal,
    val fourInARow: FourInARowMode = FourInARowMode.TwoPlayer,
    val dotsAndBoxes: DotsAndBoxesSize = DotsAndBoxesSize.Small,
    val wordSearch: WordSearchDifficulty = WordSearchDifficulty.Normal,
    val codeBreaker: CodeBreakerDifficulty = CodeBreakerDifficulty.Easy,
    val sudoku: SudokuDifficulty = SudokuDifficulty.Mini,
    val bubbleWrap: BubbleWrapSize = BubbleWrapSize.Standard,
    val timingStack: TimingStackSpeed = TimingStackSpeed.Normal,
    val mazeRunner: MazeSize = MazeSize.Medium,
    val anagramTiles: AnagramLength = AnagramLength.Mixed,
    val mancala: MancalaMode = MancalaMode.TwoPlayer,
    val simonSays: SimonSpeed = SimonSpeed.Normal
)

fun defaultSetupChoice(game: GameId): GameSetupChoice = GameSetupChoice()
