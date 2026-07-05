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
    val stackBestLayers: Int = 0,
    val mazeBestSecs: Long = 0L,
    val anagramBestSolved: Int = 0,
    val simonBestRound: Int = 0,
    val waterSortBestMoves: Int = 0,
    val nutsBestMoves: Int = 0,
    val colorFillWins: Int = 0,
    val blocksBestScore: Int = 0,
    val escapeLevelsBeaten: Int = 0,
    val paintBestSwipes: Int = 0,
    val flappyBestScore: Int = 0,
    val blockFillBestScore: Int = 0,
    val mergeChainBest: Int = 0,
    val crossMathSolved: Int = 0,
    val numberConnectWins: Int = 0,
    val solitaireWins: Int = 0,
    val warWins: Int = 0,
    val blackjackWins: Int = 0,
    val dominoWins: Int = 0,
    val checkersWins: Int = 0,
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
    const val XP_STACK_RUN = 10
    const val XP_MAZE_WIN = 20
    const val XP_ANAGRAM_ROUND = 15
    const val XP_SIMON_RUN = 10
    const val XP_SORT_WIN = 20
    const val XP_FILL_WIN = 15
    const val XP_BLOCKS_RUN = 10
    const val XP_ESCAPE_LEVEL = 20
    const val XP_PAINT_WIN = 15
    const val XP_FLAPPY_RUN = 10
    const val XP_BLOCKFILL_RUN = 10
    const val XP_CROSSMATH_WIN = 15
    const val XP_CONNECT_WIN = 20

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

    /** Epoch day (UTC) on which the daily challenge was last completed; 0 = never. */
    var dailyDoneDay: Long by mutableStateOf(prefs.getLong("daily_done_day", 0L))
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
        stackBestLayers = prefs.getInt("stack_best", 0),
        mazeBestSecs = prefs.getLong("maze_best_secs", 0L),
        anagramBestSolved = prefs.getInt("anagram_best", 0),
        simonBestRound = prefs.getInt("simon_best", 0),
        waterSortBestMoves = prefs.getInt("water_best_moves", 0),
        nutsBestMoves = prefs.getInt("nuts_best_moves", 0),
        colorFillWins = prefs.getInt("fill_wins", 0),
        blocksBestScore = prefs.getInt("blocks_best", 0),
        escapeLevelsBeaten = prefs.getInt("escape_levels", 0),
        paintBestSwipes = prefs.getInt("paint_best_swipes", 0),
        flappyBestScore = prefs.getInt("flappy_best", 0),
        blockFillBestScore = prefs.getInt("blockfill_best", 0),
        mergeChainBest = prefs.getInt("mergechain_best", 0),
        crossMathSolved = prefs.getInt("crossmath_solved", 0),
        numberConnectWins = prefs.getInt("connect_wins", 0),
        solitaireWins = prefs.getInt("solitaire_wins", 0),
        warWins = prefs.getInt("war_wins", 0),
        blackjackWins = prefs.getInt("blackjack_wins", 0),
        dominoWins = prefs.getInt("domino_wins", 0),
        checkersWins = prefs.getInt("checkers_wins", 0),
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

    fun recordStackRun(layers: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterScore(layers, cur.stackBestLayers)) layers else cur.stackBestLayers
        val newXp = cur.totalXp + if (layers > 0) ScoreLogic.XP_STACK_RUN else 0
        prefs.edit().putInt("stack_best", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(stackBestLayers = newBest, totalXp = newXp)
    }

    fun recordMazeWin(timeSecs: Long) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterTime(timeSecs, cur.mazeBestSecs)) timeSecs else cur.mazeBestSecs
        val newXp = cur.totalXp + ScoreLogic.XP_MAZE_WIN
        prefs.edit().putLong("maze_best_secs", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(mazeBestSecs = newBest, totalXp = newXp)
    }

    fun recordWaterSortWin(moves: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterMoves(moves, cur.waterSortBestMoves)) moves else cur.waterSortBestMoves
        val newXp = cur.totalXp + ScoreLogic.XP_SORT_WIN
        prefs.edit().putInt("water_best_moves", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(waterSortBestMoves = newBest, totalXp = newXp)
    }

    fun recordNutsWin(moves: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterMoves(moves, cur.nutsBestMoves)) moves else cur.nutsBestMoves
        val newXp = cur.totalXp + ScoreLogic.XP_SORT_WIN
        prefs.edit().putInt("nuts_best_moves", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(nutsBestMoves = newBest, totalXp = newXp)
    }

    fun recordColorFillWin() {
        val cur = scores
        val newWins = cur.colorFillWins + 1
        val newXp = cur.totalXp + ScoreLogic.XP_FILL_WIN
        prefs.edit().putInt("fill_wins", newWins).putInt("total_xp", newXp).apply()
        scores = cur.copy(colorFillWins = newWins, totalXp = newXp)
    }

    fun recordBlocksRun(score: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterScore(score, cur.blocksBestScore)) score else cur.blocksBestScore
        val newXp = cur.totalXp + if (score > 0) ScoreLogic.XP_BLOCKS_RUN else 0
        prefs.edit().putInt("blocks_best", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(blocksBestScore = newBest, totalXp = newXp)
    }

    /** Called on every merge with the running score; XP only on new bests to avoid farming. */
    fun recordMergeChainScore(score: Int) {
        val cur = scores
        if (!ScoreLogic.isBetterScore(score, cur.mergeChainBest)) return
        prefs.edit().putInt("mergechain_best", score).apply()
        scores = cur.copy(mergeChainBest = score)
    }

    fun recordCrossMathWin() {
        val cur = scores
        val newCount = cur.crossMathSolved + 1
        val newXp = cur.totalXp + ScoreLogic.XP_CROSSMATH_WIN
        prefs.edit().putInt("crossmath_solved", newCount).putInt("total_xp", newXp).apply()
        scores = cur.copy(crossMathSolved = newCount, totalXp = newXp)
    }

    fun recordNumberConnectWin() {
        val cur = scores
        val newCount = cur.numberConnectWins + 1
        val newXp = cur.totalXp + ScoreLogic.XP_CONNECT_WIN
        prefs.edit().putInt("connect_wins", newCount).putInt("total_xp", newXp).apply()
        scores = cur.copy(numberConnectWins = newCount, totalXp = newXp)
    }

    private fun bumpWinCounter(key: String, current: Int, xp: Int, update: (HighScores, Int) -> HighScores) {
        val cur = scores
        val newCount = current + 1
        val newXp = cur.totalXp + xp
        prefs.edit().putInt(key, newCount).putInt("total_xp", newXp).apply()
        scores = update(cur.copy(totalXp = newXp), newCount)
    }

    fun recordSolitaireWin() = bumpWinCounter("solitaire_wins", scores.solitaireWins, 40) { s, n -> s.copy(solitaireWins = n) }
    fun recordWarWin() = bumpWinCounter("war_wins", scores.warWins, 15) { s, n -> s.copy(warWins = n) }
    fun recordBlackjackWin() = bumpWinCounter("blackjack_wins", scores.blackjackWins, 5) { s, n -> s.copy(blackjackWins = n) }
    fun recordDominoWin() = bumpWinCounter("domino_wins", scores.dominoWins, 20) { s, n -> s.copy(dominoWins = n) }
    fun recordCheckersWin() = bumpWinCounter("checkers_wins", scores.checkersWins, 30) { s, n -> s.copy(checkersWins = n) }

    fun recordEscapeLevel() {
        val cur = scores
        val newCount = cur.escapeLevelsBeaten + 1
        val newXp = cur.totalXp + ScoreLogic.XP_ESCAPE_LEVEL
        prefs.edit().putInt("escape_levels", newCount).putInt("total_xp", newXp).apply()
        scores = cur.copy(escapeLevelsBeaten = newCount, totalXp = newXp)
    }

    fun recordMazePaintWin(swipes: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterMoves(swipes, cur.paintBestSwipes)) swipes else cur.paintBestSwipes
        val newXp = cur.totalXp + ScoreLogic.XP_PAINT_WIN
        prefs.edit().putInt("paint_best_swipes", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(paintBestSwipes = newBest, totalXp = newXp)
    }

    fun recordFlappyRun(score: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterScore(score, cur.flappyBestScore)) score else cur.flappyBestScore
        val newXp = cur.totalXp + if (score > 0) ScoreLogic.XP_FLAPPY_RUN else 0
        prefs.edit().putInt("flappy_best", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(flappyBestScore = newBest, totalXp = newXp)
    }

    fun recordBlockFillRun(score: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterScore(score, cur.blockFillBestScore)) score else cur.blockFillBestScore
        val newXp = cur.totalXp + if (score > 0) ScoreLogic.XP_BLOCKFILL_RUN else 0
        prefs.edit().putInt("blockfill_best", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(blockFillBestScore = newBest, totalXp = newXp)
    }

    fun recordSimonRun(rounds: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterScore(rounds, cur.simonBestRound)) rounds else cur.simonBestRound
        val newXp = cur.totalXp + if (rounds > 0) ScoreLogic.XP_SIMON_RUN else 0
        prefs.edit().putInt("simon_best", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(simonBestRound = newBest, totalXp = newXp)
    }

    fun recordAnagramRound(solved: Int) {
        val cur = scores
        val newBest = if (ScoreLogic.isBetterScore(solved, cur.anagramBestSolved)) solved else cur.anagramBestSolved
        val newXp = cur.totalXp + if (solved > 0) ScoreLogic.XP_ANAGRAM_ROUND else 0
        prefs.edit().putInt("anagram_best", newBest).putInt("total_xp", newXp).apply()
        scores = cur.copy(anagramBestSolved = newBest, totalXp = newXp)
    }

    /** Awards the daily bonus once per epoch day. Returns true if the bonus was granted. */
    fun markDailyComplete(epochDay: Long, bonusXp: Int): Boolean {
        if (dailyDoneDay == epochDay) return false
        val newXp = scores.totalXp + bonusXp
        prefs.edit().putLong("daily_done_day", epochDay).putInt("total_xp", newXp).apply()
        dailyDoneDay = epochDay
        scores = scores.copy(totalXp = newXp)
        return true
    }

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
        dailyDoneDay = 0L
    }
}
