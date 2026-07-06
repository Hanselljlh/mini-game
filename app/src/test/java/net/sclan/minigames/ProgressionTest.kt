package net.sclan.minigames

import net.sclan.minigames.data.Achievements
import net.sclan.minigames.data.HighScores
import net.sclan.minigames.ui.DailyChallenge
import net.sclan.minigames.ui.GameId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {

    // --- Daily challenge ---

    @Test fun dailyPickIsDeterministic() {
        val day = 20_640L
        assertEquals(DailyChallenge.gameForDay(day), DailyChallenge.gameForDay(day))
    }

    @Test fun dailyPickAlwaysValid() {
        (0L..400L).forEach { day ->
            val game = DailyChallenge.gameForDay(day)
            assertTrue(game in GameId.entries)
        }
    }

    @Test fun dailyPickCyclesThroughAllPlayableGames() {
        val playable = DailyChallenge.playable
        val picked = (0L until playable.size.toLong()).map { DailyChallenge.gameForDay(it) }.toSet()
        assertEquals(playable.toSet(), picked)
    }

    @Test fun dailyPickNeverSelectsScorelessToys() {
        val excluded = setOf(GameId.SandFall, GameId.FidgetSpinner, GameId.ChalkDoodle)
        (0L..500L).forEach { day ->
            assertTrue(DailyChallenge.gameForDay(day) !in excluded)
        }
    }

    @Test fun dailyPickHandlesNegativeDays() {
        assertTrue(DailyChallenge.gameForDay(-5L) in GameId.entries)
    }

    // --- Achievements ---

    @Test fun freshProfileHasNoAchievements() {
        assertTrue(Achievements.unlocked(HighScores()).isEmpty())
    }

    @Test fun achievementIdsAreUnique() {
        val ids = Achievements.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun firstGameUnlocksFirstSteps() {
        val unlocked = Achievements.unlocked(HighScores(gamesPlayed = 1))
        assertTrue(unlocked.any { it.id == "first_game" })
        assertEquals(1, unlocked.size)
    }

    @Test fun tileAchievementsStack() {
        val unlocked = Achievements.unlocked(HighScores(best2048Tile = 2048)).map { it.id }
        assertTrue("tile_1024" in unlocked)
        assertTrue("tile_2048" in unlocked)
    }

    @Test fun reactionAchievementRequiresNonZero() {
        // 0 means "never played" — must not unlock the sub-250ms achievement
        val none = Achievements.unlocked(HighScores(reactionBestMs = 0L)).map { it.id }
        assertTrue("reaction_fast" !in none)
        val fast = Achievements.unlocked(HighScores(reactionBestMs = 200L)).map { it.id }
        assertTrue("reaction_fast" in fast)
    }

    @Test fun codeBreakerFastRequiresWin() {
        val none = Achievements.unlocked(HighScores(codeBestGuesses = 0)).map { it.id }
        assertTrue("code_fast" !in none)
        val fast = Achievements.unlocked(HighScores(codeBestGuesses = 4)).map { it.id }
        assertTrue("code_fast" in fast)
        assertTrue("code_first" in fast)
    }

    @Test fun levelAchievements() {
        val ids = Achievements.unlocked(HighScores(totalXp = 950)).map { it.id }
        assertTrue("level_5" in ids)   // level 10 needs 900+ xp: 950/100+1 = 10
        assertTrue("level_10" in ids)
    }
}
