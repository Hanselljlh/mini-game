package net.sclan.minigames.ui

/**
 * Deterministic daily pick — same for everyone on the same day, no server needed.
 * epochDay is days since 1970-01-01 UTC (System.currentTimeMillis() / 86_400_000).
 */
object DailyChallenge {

    const val BONUS_XP = 50

    fun todayEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    /** Games that report a completion event and can therefore be a daily pick. */
    val playable: List<GameId> get() = GameId.entries.filter {
        it != GameId.SandFall && it != GameId.FidgetSpinner && it != GameId.ChalkDoodle
    }

    fun gameForDay(epochDay: Long): GameId {
        val games = playable
        val index = ((epochDay % games.size) + games.size) % games.size
        return games[index.toInt()]
    }
}
