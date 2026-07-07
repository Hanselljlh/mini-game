package net.sclan.minigames.data

/** Pure logic — achievements are derived from local stats, never networked. */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val check: (HighScores) -> Boolean
)

object Achievements {

    val all: List<Achievement> = listOf(
        Achievement("first_game", "First Steps", "Play your first game") { it.gamesPlayed >= 1 },
        Achievement("ten_games", "Regular", "Play 10 games") { it.gamesPlayed >= 10 },
        Achievement("fifty_games", "Arcade Devotee", "Play 50 games") { it.gamesPlayed >= 50 },
        Achievement("level_5", "Rising Star", "Reach level 5") { ScoreLogic.levelForXp(it.totalXp) >= 5 },
        Achievement("level_10", "Arcade Legend", "Reach level 10") { ScoreLogic.levelForXp(it.totalXp) >= 10 },
        Achievement("tile_1024", "Tile Apprentice", "Reach a 1024 tile in Tile Merge") { it.best2048Tile >= 1024 },
        Achievement("tile_2048", "Merge Master", "Reach a 2048 tile in Tile Merge") { it.best2048Tile >= 2048 },
        Achievement("ms_first", "Sweeper", "Win a game of Minesweeper") { it.minesweeperWins >= 1 },
        Achievement("ms_ten", "Bomb Squad", "Win 10 games of Minesweeper") { it.minesweeperWins >= 10 },
        Achievement("memory_sharp", "Photographic", "Clear Memory Match in 12 moves or fewer") { it.memoryBestMoves in 1..12 },
        Achievement("reaction_fast", "Lightning Reflexes", "Average under 250 ms in Reaction Tap") { it.reactionBestMs in 1..249 },
        Achievement("snake_10", "Snake Charmer", "Eat 10 food in one Snake run") { it.snakeBestScore >= 10 },
        Achievement("snake_25", "Constrictor", "Eat 25 food in one Snake run") { it.snakeBestScore >= 25 },
        Achievement("word_first", "Word Hunter", "Complete a Word Search") { it.wordSearchBestSecs > 0 },
        Achievement("code_first", "Code Cracker", "Crack a code in Code Breaker") { it.codeBestGuesses > 0 },
        Achievement("code_fast", "Mastermind", "Crack a code in 5 guesses or fewer") { it.codeBestGuesses in 1..5 },
        Achievement("sudoku_first", "Number Sage", "Solve a Sudoku puzzle") { it.sudokuWins >= 1 },
        Achievement("sudoku_five", "Grid Guru", "Solve 5 Sudoku puzzles") { it.sudokuWins >= 5 },
        Achievement("stack_perfect", "Master Builder", "Finish a 12-layer Timing Stack tower") { it.stackBestLayers >= 12 },
        Achievement("maze_fast", "Speed Runner", "Escape a maze in under 20 seconds") { it.mazeBestSecs in 1..19 },
        Achievement("simon_10", "Copycat", "Reach round 10 in Simon Says") { it.simonBestRound >= 10 }
    )

    fun unlocked(scores: HighScores): List<Achievement> = all.filter { it.check(scores) }
}
