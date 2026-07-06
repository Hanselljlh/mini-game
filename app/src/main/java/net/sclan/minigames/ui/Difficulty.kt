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
    SimonSays("Simon Says", "Watch the pads light up, then repeat the growing sequence."),
    WaterSort("Water Sort", "Pour colored water between tubes until every tube is one color."),
    NutsAndBolts("Nuts & Bolts", "Move colored nuts between bolts until each bolt matches."),
    ColorFill("Multi-Color Fill", "Flood the board into a single color before moves run out."),
    ColorBlocks("Color Blocks", "Tap groups of matching blocks to clear the board for big scores."),
    Escape("Escape", "Slide the blockers out of the way and drive the red block to the exit."),
    MazePaint("Maze Paint", "Slide through the maze and paint every square you touch."),
    FlappyJump("Flappy Jump", "Tap to flap and thread the gaps. One touch ends the run."),
    SandFall("Sand Fall", "Pour rainbow sand and watch it pile, slide, and settle."),
    BlockFill("Block Fill", "Place pieces on the board and clear full rows and columns."),
    MergeChain("Merge Chain", "Link matching numbers into chains and merge them ever higher."),
    CrossMath("Cross Math", "Place digits so every equation works across and down."),
    NumberConnect("Number Connect", "Retrace the hidden 1-to-N path through the grid."),
    Solitaire("Solitaire", "Classic Klondike — build the four foundations from Ace to King."),
    War("War", "Flip cards head-to-head. Highest card takes both."),
    Blackjack("Blackjack", "Hit or stand — get closest to 21 without busting."),
    Dominoes("Dominoes", "Match tiles to the chain ends and empty your hand first."),
    Checkers("Checkers", "Jump, capture, and crown kings against the bot."),
    Ludo("Ludo", "Race all four tokens home — roll sixes, capture rivals, play it safe on stars."),
    WordRescue("Word Rescue", "Guess letters to save the word before all six balloons pop."),
    WordGuess("Word Guess", "Crack the 5-letter word in six color-coded tries."),
    SlidingPuzzle("Sliding Puzzle", "Slide the numbered tiles back into order."),
    Pong("Pong Duel", "Classic paddle battle — vs bot or two players on one screen."),
    PenaltyKicks("Penalty Kicks", "Time your shot past the diving keeper — five shots per round."),
    FidgetSpinner("Fidget Spinner", "Flick it. Watch it spin. Feel better."),
    ChalkDoodle("Chalk Doodle", "A pocket chalkboard for scribbling whatever you like.")
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

enum class WaterSortDifficulty(val label: String, val colors: Int) {
    Easy("Easy • 4 colors", 4),
    Normal("Normal • 6 colors", 6),
    Hard("Hard • 8 colors", 8)
}

enum class ColorFillDifficulty(val label: String, val gridSize: Int, val colors: Int, val moveLimit: Int) {
    Easy("Easy 10×10", 10, 4, 18),
    Normal("Normal 12×12", 12, 5, 22),
    Hard("Hard 14×14", 14, 6, 25)
}

enum class ColorBlocksDifficulty(val label: String, val rows: Int, val cols: Int, val colors: Int) {
    Easy("Easy • 4 colors", 9, 8, 4),
    Normal("Normal • 5 colors", 10, 9, 5),
    Hard("Hard • 6 colors", 12, 10, 6)
}

enum class EscapePack(val label: String, val firstLevel: Int, val count: Int) {
    Rookie("Rookie • levels 1–2", 0, 2),
    Driver("Driver • levels 3–4", 2, 2),
    Expert("Expert • levels 5–6", 4, 2)
}

enum class FlappyDifficulty(val label: String, val speed: Float, val gap: Float) {
    Easy("Easy", 0.006f, 0.34f),
    Normal("Normal", 0.008f, 0.30f),
    Hard("Hard", 0.010f, 0.26f)
}

enum class SandBrush(val label: String, val radius: Int) {
    Fine("Fine stream", 0),
    Normal("Steady pour", 1),
    Wide("Bucket", 2)
}

enum class BlockFillMode(val label: String) {
    Classic("Classic 10×10")
}

enum class MergeChainMode(val label: String) {
    Classic("Classic 5×6")
}

enum class CrossMathDifficulty(val label: String, val ops: List<Char>, val decoys: Int) {
    Easy("Easy • + only", listOf('+'), 0),
    Normal("Normal • + −", listOf('+', '−'), 2),
    Hard("Hard • + − ×", listOf('+', '−', '×'), 3)
}

enum class NumberConnectDifficulty(val label: String, val gridSize: Int, val revealEvery: Int) {
    Easy("Easy 5×5", 5, 3),
    Normal("Normal 6×6", 6, 4),
    Hard("Hard 7×7", 7, 5)
}

/** Shared single-mode marker for the classic card and board games. */
enum class ClassicMode(val label: String) {
    Classic("Classic rules")
}

enum class LudoMode(val label: String, val players: Int) {
    VsBot("You vs Bot", 2),
    TwoPlayer("2 Players", 2),
    FourPlayer("4 Players", 4)
}

enum class SlidingSize(val label: String, val n: Int) {
    Mini("Mini 3×3", 3),
    Classic("Classic 4×4", 4),
    Large("Large 5×5", 5)
}

enum class PongMode(val label: String, val speed: Float, val botStep: Float) {
    VsBot("Vs Bot", 1f, 0.006f),
    FastBot("Fast Bot", 1.4f, 0.009f),
    TwoPlayer("2 Players", 1.1f, 0f)
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
    val simonSays: SimonSpeed = SimonSpeed.Normal,
    val waterSort: WaterSortDifficulty = WaterSortDifficulty.Normal,
    val nutsAndBolts: WaterSortDifficulty = WaterSortDifficulty.Normal,
    val colorFill: ColorFillDifficulty = ColorFillDifficulty.Normal,
    val colorBlocks: ColorBlocksDifficulty = ColorBlocksDifficulty.Normal,
    val escape: EscapePack = EscapePack.Rookie,
    val mazePaint: MazeSize = MazeSize.Medium,
    val flappyJump: FlappyDifficulty = FlappyDifficulty.Normal,
    val sandFall: SandBrush = SandBrush.Normal,
    val blockFill: BlockFillMode = BlockFillMode.Classic,
    val mergeChain: MergeChainMode = MergeChainMode.Classic,
    val crossMath: CrossMathDifficulty = CrossMathDifficulty.Normal,
    val numberConnect: NumberConnectDifficulty = NumberConnectDifficulty.Normal,
    val classic: ClassicMode = ClassicMode.Classic,
    val ludo: LudoMode = LudoMode.VsBot,
    val slidingPuzzle: SlidingSize = SlidingSize.Classic,
    val pong: PongMode = PongMode.VsBot
)

fun defaultSetupChoice(game: GameId): GameSetupChoice = GameSetupChoice()
