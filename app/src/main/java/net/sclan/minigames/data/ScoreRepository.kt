package net.sclan.minigames.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class HighScores(
    val best2048Tile: Int = 0,
    val best2048Score: Int = 0,
    val minesweeperWins: Int = 0,
    val minesweeperBestTimeSecs: Long = 0L,
    val memoryBestMoves: Int = 0,
    val reactionBestMs: Long = 0L,
    val snakeBestScore: Int = 0,
    val wordSearchBestSecs: Long = 0L,
    val codeBestGuesses: Int = 0,
    val sudokuWins: Int = 0,
    val totalXp: Int = 0,
    val gamesPlayed: Int = 0
)

/** Pure logic — no Android/Compose deps, fully unit-testable. */
object ScoreLogic {
    const val XP_2048_WIN = 30
    const val XP_MINESWEEPER_WIN = 25
    const val XP_MEMORY_WIN = 15
    const val XP_REACTION_FINISH = 10
    const val XP_SNAKE_RUN = 10
    const val XP_WORDSEARCH_WIN = 20
    const val XP_CODEBREAKER_WIN = 20
    const val XP_SUDOKU_WIN = 40
    const val XP_DUEL_FINISH = 15
    const val XP_BUBBLE_SHEET = 5

    fun isBetterTile(new: Int, best: Int): Boolean = new > best
    fun isBetterScore(new: Int, best: Int): Boolean = new > best
    fun isBetterTime(newSecs: Long, bestSecs: Long): Boolean = bestSecs == 0L || newSecs < bestSecs
    fun isBetterMoves(newMoves: Int, bestMoves: Int): Boolean = bestMoves == 0 || newMoves < bestMoves
    fun isBetterReaction(newMs: Long, bestMs: Long): Boolean = bestMs == 0L || newMs < bestMs

    fun tileLabel(tile: Int): String = if (tile == 0) "—" else tile.toString()
    fun timeLabel(secs: Long): String = if (secs == 0L) "—" else "${secs}s"
    fun movesLabel(moves: Int): String = if (moves == 0) "—" else "$moves moves"
    fun reactionLabel(ms: Long): String = if (ms == 0L) "—" else "$ms ms"

    /** Level 1 at 0 XP, +1 level every 100 XP. */
    fun levelForXp(xp: Int): Int = xp / 100 + 1
    fun xpIntoLevel(xp: Int): Int = xp % 100

    /** Most-recent-first recents list capped at [max], no duplicates. */
    fun updatedRecents(current: List<String>, played: String, max: Int = 5): List<String> =
        (listOf(played) + current.filter { it != played }).take(max)
}

class ScoreRepository(context: Context) {
    private val prefs = context.getSharedPreferences("pmg_scores", Context.MODE_PRIVATE)

    var scores: HighScores by mutableStateOf(loadScores())
        private set

    /** GameId names marked as favorite. */
    var favorites: Set<String> by mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet())
        private set

    /** GameId names, most recently played first. */
    var recents: List<String> by mutableStateOf(loadRecents())
        private set

    private fun loadScores() = HighScores(
        best2048Tile = prefs.getInt("best_tile", 0),
        best2048Score = prefs.getInt("best_score", 0),
        minesweeperWins = prefs.getInt("ms_wins", 0),
        minesweeperBestTimeSecs = prefs.getLong("ms_best_time", 0L),
        memoryBestMoves = prefs.getInt("mem_best_moves", 0),
        reactionBestMs = prefs.getLong("react_best_ms", 0L),
        snakeBestScore = prefs.getInt("snake_best", 0),
        wordSearchBestSecs = prefs.getLong("ws_best_secs", 0L),
        codeBestGuesses = prefs.getInt("code_best_guesses", 0),
        sudokuWins = prefs.getInt("sudoku_wins", 0),
        totalXp = prefs.getInt("total_xp", 0),
        gamesPlayed = prefs.getInt("games_played", 0)
    )

    private fun loadRecents(): List<String> =
        prefs.getString("recents", "")!!.split(',').filter { it.isNotBlank() }

    fun toggleFavorite(gameId: String) {
        val next = if (gameId in favorites) favorites - gameId else favorites + gameId
        prefs.edit().putStringSet("favorites", next).apply()
        favorites = next
    }

    fun recordPlayed(gameId: String) {
        val next = ScoreLogic.updatedRecents(recents, gameId)
        val played = scores.gamesPlayed + 1
        prefs.edit().putString("recents", next.joinToString(",")).putInt("games_played", played).apply()
        recents = next
        scores = scores.copy(gamesPlayed = played)
    }

    fun tryUpdateBest2048(tile: Int, score: Int) {
        val cur = scores
        val newTile = if (ScoreLogic.isBetterTile(tile, cur.best2048Tile)) tile else cur.best2048Tile
        val newScore = if (ScoreLogic.isBetterScore(score, cur.best2048Score)) score else cur.best2048Score
        if (newTile == cur.best2048Tile && newScore == cur.best2048Score) return
        prefs.edit().putInt("best_tile", newTile).putInt("best_score", newScore).apply()
        scores = cur.copy(best2048Tile = newTile, best2048Score = newScore)
    }

    fun record2048Win() = addXp(ScoreLogic.XP_2048_WIN)

    fun recordMinesweeperWin(timeSecs: Long) {
        val cur = scores
        val newWins = cur.minesweeperWins + 1
        val newBest = if (ScoreLogic.isBetterTime(timeSecs, cur.minesweeperBestTimeSecs))
            timeSecs else cur.minesweeperBestTimeSecs
        val newXp = cur.totalXp + ScoreLogic.XP_MINESWEEPER_WIN
        prefs.edit().putInt("ms_wins", newWins).putLong("ms_best_time", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(minesweeperWins = newWins, minesweeperBestTimeSecs = newBest, totalXp = newXp)
    }

    fun recordMemoryWin(moves: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterMoves(moves, cur.memoryBestMoves)) moves else cur.memoryBestMoves
        val newXp = cur.totalXp + ScoreLogic.XP_MEMORY_WIN
        prefs.edit().putInt("mem_best_moves", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(memoryBestMoves = newBest, totalXp = newXp)
    }

    fun recordReactionResult(avgMs: Long) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterReaction(avgMs, cur.reactionBestMs)) avgMs else cur.reactionBestMs
        val newXp = cur.totalXp + ScoreLogic.XP_REACTION_FINISH
        prefs.edit().putLong("react_best_ms", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(reactionBestMs = newBest, totalXp = newXp)
    }

    fun recordSnakeRun(score: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterScore(score, cur.snakeBestScore)) score else cur.snakeBestScore
        val newXp = cur.totalXp + if (score > 0) ScoreLogic.XP_SNAKE_RUN else 0
        prefs.edit().putInt("snake_best", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(snakeBestScore = newBest, totalXp = newXp)
    }

    fun recordWordSearchWin(timeSecs: Long) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterTime(timeSecs, cur.wordSearchBestSecs)) timeSecs else cur.wordSearchBestSecs
        val newXp = cur.totalXp + ScoreLogic.XP_WORDSEARCH_WIN
        prefs.edit().putLong("ws_best_secs", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(wordSearchBestSecs = newBest, totalXp = newXp)
    }

    fun recordCodeBreakerWin(guesses: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterMoves(guesses, cur.codeBestGuesses)) guesses else cur.codeBestGuesses
        val newXp = cur.totalXp + ScoreLogic.XP_CODEBREAKER_WIN
        prefs.edit().putInt("code_best_guesses", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(codeBestGuesses = newBest, totalXp = newXp)
    }

    fun recordSudokuWin() {
        val cur = scores
        val newWins = cur.sudokuWins + 1
        val newXp = cur.totalXp + ScoreLogic.XP_SUDOKU_WIN
        prefs.edit().putInt("sudoku_wins", newWins).putInt("total_xp", newXp).apply()
        scores = cur.copy(sudokuWins = newWins, totalXp = newXp)
    }

    fun recordDuelFinished() = addXp(ScoreLogic.XP_DUEL_FINISH)

    fun recordBubbleSheet() = addXp(ScoreLogic.XP_BUBBLE_SHEET)

    private fun addXp(amount: Int) {
        val newXp = scores.totalXp + amount
        prefs.edit().putInt("total_xp", newXp).apply()
        scores = scores.copy(totalXp = newXp)
    }

    /** Privacy control: wipe every locally stored score, favorite, and stat. */
    fun deleteAllData() {
        prefs.edit().clear().apply()
        scores = HighScores()
        favorites = emptySet()
        recents = emptyList()
    }
}
