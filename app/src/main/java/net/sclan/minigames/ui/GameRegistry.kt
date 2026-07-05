package net.sclan.minigames.ui

enum class GameCategory(val label: String) {
    Brain("Brain & Logic"),
    Puzzle("Sort & Fill"),
    Words("Words"),
    Cards("Cards & Classics"),
    Arcade("Arcade"),
    Duel("Local Duel"),
    Relax("Relax Toys")
}

data class GameMeta(
    val id: GameId,
    val category: GameCategory,
    val players: String,
    val estTime: String,
    val subtitle: String
)

object GameRegistry {

    val games: List<GameMeta> = listOf(
        GameMeta(GameId.TileMerge, GameCategory.Brain, "Solo", "5 min", "Slide and merge matching number tiles"),
        GameMeta(GameId.Minesweeper, GameCategory.Brain, "Solo", "5 min", "Reveal safe squares and flag hidden mines"),
        GameMeta(GameId.CodeBreaker, GameCategory.Brain, "Solo", "5 min", "Crack the secret color code with logic"),
        GameMeta(GameId.Sudoku, GameCategory.Brain, "Solo", "10 min", "Every row, column, and box — each number once"),
        GameMeta(GameId.WordSearch, GameCategory.Words, "Solo", "5 min", "Find every hidden word in the letter grid"),
        GameMeta(GameId.MemoryMatch, GameCategory.Cards, "Solo", "2 min", "Flip cards and find every matching pair"),
        GameMeta(GameId.ReactionTap, GameCategory.Arcade, "Solo", "30 sec", "Wait for green, then tap as fast as you can"),
        GameMeta(GameId.Snake, GameCategory.Arcade, "Solo", "3 min", "Grow the snake, dodge the walls and yourself"),
        GameMeta(GameId.TicTacToe, GameCategory.Duel, "1–2 players", "2 min", "Pass-and-play, easy bot, or smart bot"),
        GameMeta(GameId.FourInARow, GameCategory.Duel, "1–2 players", "5 min", "Drop discs and connect four to win"),
        GameMeta(GameId.DotsAndBoxes, GameCategory.Duel, "2 players", "5 min", "Close boxes with lines — most boxes wins"),
        GameMeta(GameId.BubbleWrap, GameCategory.Relax, "Solo", "1 min", "Pop every bubble. No goal. Pure calm."),
        GameMeta(GameId.TimingStack, GameCategory.Arcade, "Solo", "2 min", "Tap at the right moment to stack blocks high"),
        GameMeta(GameId.MazeRunner, GameCategory.Brain, "Solo", "3 min", "Slide through the maze to the exit"),
        GameMeta(GameId.AnagramTiles, GameCategory.Words, "Solo", "3 min", "Unscramble letters to rebuild the word"),
        GameMeta(GameId.Mancala, GameCategory.Duel, "1–2 players", "5 min", "Sow seeds, land in your store, capture big"),
        GameMeta(GameId.SimonSays, GameCategory.Brain, "Solo", "2 min", "Repeat the flashing sequence as it grows"),
        GameMeta(GameId.WaterSort, GameCategory.Puzzle, "Solo", "5 min", "Pour water until every tube is one color"),
        GameMeta(GameId.NutsAndBolts, GameCategory.Puzzle, "Solo", "5 min", "Sort colored nuts onto matching bolts"),
        GameMeta(GameId.ColorFill, GameCategory.Puzzle, "Solo", "3 min", "Flood the board into one color in limited moves"),
        GameMeta(GameId.ColorBlocks, GameCategory.Puzzle, "Solo", "3 min", "Clear matching block groups — bigger is better"),
        GameMeta(GameId.Escape, GameCategory.Brain, "Solo", "5 min", "Slide blockers aside and free the red block"),
        GameMeta(GameId.MazePaint, GameCategory.Arcade, "Solo", "3 min", "Slide and paint every square of the maze"),
        GameMeta(GameId.FlappyJump, GameCategory.Arcade, "Solo", "2 min", "Tap to flap through the gaps"),
        GameMeta(GameId.SandFall, GameCategory.Relax, "Solo", "∞", "Pour rainbow sand, watch it settle"),
        GameMeta(GameId.BlockFill, GameCategory.Puzzle, "Solo", "5 min", "Place pieces, clear lines, don't run out of room"),
        GameMeta(GameId.MergeChain, GameCategory.Brain, "Solo", "5 min", "Chain matching numbers into ever-bigger merges"),
        GameMeta(GameId.CrossMath, GameCategory.Brain, "Solo", "3 min", "Make every equation work, across and down"),
        GameMeta(GameId.NumberConnect, GameCategory.Brain, "Solo", "5 min", "Retrace the hidden 1-to-N path")
    )

    fun meta(id: GameId): GameMeta = games.first { it.id == id }

    fun search(query: String): List<GameMeta> {
        val q = query.trim()
        if (q.isEmpty()) return games
        return games.filter {
            it.id.title.contains(q, ignoreCase = true) ||
                it.subtitle.contains(q, ignoreCase = true) ||
                it.category.label.contains(q, ignoreCase = true)
        }
    }
}
