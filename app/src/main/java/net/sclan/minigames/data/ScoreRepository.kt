package net.sclan.minigames.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class HighScores(
    val best2048Tile: Int = 0,
    val best2048Score: Int = 0,
    val minesweeperWins: Int = 0,
    val minesweeperBestTimeSecs: Long = 0L
)

/** Pure logic — no Android/Compose deps, fully unit-testable. */
object ScoreLogic {
    fun isBetterTile(new: Int, best: Int): Boolean = new > best
    fun isBetterScore(new: Int, best: Int): Boolean = new > best
    fun isBetterTime(newSecs: Long, bestSecs: Long): Boolean = bestSecs == 0L || newSecs < bestSecs
    fun tileLabel(tile: Int): String = if (tile == 0) "—" else tile.toString()
    fun timeLabel(secs: Long): String = if (secs == 0L) "—" else "${secs}s"
}

class ScoreRepository(context: Context) {
    private val prefs = context.getSharedPreferences("pmg_scores", Context.MODE_PRIVATE)

    var scores: HighScores by mutableStateOf(load())
        private set

    private fun load() = HighScores(
        best2048Tile = prefs.getInt("best_tile", 0),
        best2048Score = prefs.getInt("best_score", 0),
        minesweeperWins = prefs.getInt("ms_wins", 0),
        minesweeperBestTimeSecs = prefs.getLong("ms_best_time", 0L)
    )

    fun tryUpdateBest2048(tile: Int, score: Int) {
        val cur = scores
        val newTile = if (ScoreLogic.isBetterTile(tile, cur.best2048Tile)) tile else cur.best2048Tile
        val newScore = if (ScoreLogic.isBetterScore(score, cur.best2048Score)) score else cur.best2048Score
        if (newTile == cur.best2048Tile && newScore == cur.best2048Score) return
        prefs.edit().putInt("best_tile", newTile).putInt("best_score", newScore).apply()
        scores = cur.copy(best2048Tile = newTile, best2048Score = newScore)
    }

    fun recordMinesweeperWin(timeSecs: Long) {
        val cur = scores
        val newWins = cur.minesweeperWins + 1
        val newBest = if (ScoreLogic.isBetterTime(timeSecs, cur.minesweeperBestTimeSecs))
            timeSecs else cur.minesweeperBestTimeSecs
        prefs.edit().putInt("ms_wins", newWins).putLong("ms_best_time", newBest).apply()
        scores = cur.copy(minesweeperWins = newWins, minesweeperBestTimeSecs = newBest)
    }
}
