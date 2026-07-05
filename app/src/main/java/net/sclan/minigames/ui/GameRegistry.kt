package net.sclan.minigames.ui

enum class GameCategory(val label: String) {
    Brain("Brain & Logic"),
    Cards("Cards & Classics"),
    Arcade("Arcade"),
    Duel("Local Duel")
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
        GameMeta(
            id = GameId.TileMerge,
            category = GameCategory.Brain,
            players = "Solo",
            estTime = "5 min",
            subtitle = "Slide and merge matching number tiles"
        ),
        GameMeta(
            id = GameId.Minesweeper,
            category = GameCategory.Brain,
            players = "Solo",
            estTime = "5 min",
            subtitle = "Reveal safe squares and flag hidden mines"
        ),
        GameMeta(
            id = GameId.MemoryMatch,
            category = GameCategory.Cards,
            players = "Solo",
            estTime = "2 min",
            subtitle = "Flip cards and find every matching pair"
        ),
        GameMeta(
            id = GameId.ReactionTap,
            category = GameCategory.Arcade,
            players = "Solo",
            estTime = "30 sec",
            subtitle = "Wait for green, then tap as fast as you can"
        ),
        GameMeta(
            id = GameId.TicTacToe,
            category = GameCategory.Duel,
            players = "1–2 players",
            estTime = "2 min",
            subtitle = "Pass-and-play, easy bot, or smart bot"
        )
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
