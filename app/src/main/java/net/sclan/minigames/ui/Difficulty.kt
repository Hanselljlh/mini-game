package net.sclan.minigames.ui

enum class GameId(val title: String, val shortDescription: String) {
    TileMerge("Tile Merge", "Slide matching numbers together until you reach the target tile."),
    Minesweeper("Minesweeper", "Reveal safe squares, flag mines, and clear the board."),
    TicTacToe("Tic Tac Toe", "Line up three marks before your opponent does.")
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

data class GameSetupChoice(
    val tileMerge: TileMergeDifficulty = TileMergeDifficulty.Normal,
    val minesweeper: MinesweeperDifficulty = MinesweeperDifficulty.Normal,
    val ticTacToe: TicTacToeDifficulty = TicTacToeDifficulty.TwoPlayer
)

fun defaultSetupChoice(game: GameId): GameSetupChoice = when (game) {
    GameId.TileMerge -> GameSetupChoice(tileMerge = TileMergeDifficulty.Normal)
    GameId.Minesweeper -> GameSetupChoice(minesweeper = MinesweeperDifficulty.Normal)
    GameId.TicTacToe -> GameSetupChoice(ticTacToe = TicTacToeDifficulty.TwoPlayer)
}
