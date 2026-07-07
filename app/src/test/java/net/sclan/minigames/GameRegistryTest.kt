package net.sclan.minigames

import net.sclan.minigames.data.ScoreLogic
import net.sclan.minigames.ui.GameId
import net.sclan.minigames.ui.GameRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRegistryTest {

    @Test fun everyGameIdIsRegisteredExactlyOnce() {
        GameId.entries.forEach { id ->
            assertEquals("Registry entries for $id", 1, GameRegistry.games.count { it.id == id })
        }
        assertEquals(GameId.entries.size, GameRegistry.games.size)
    }

    @Test fun searchBlankReturnsAll() {
        assertEquals(GameRegistry.games, GameRegistry.search(""))
        assertEquals(GameRegistry.games, GameRegistry.search("   "))
    }

    @Test fun searchMatchesTitleCaseInsensitive() {
        val results = GameRegistry.search("memory")
        assertTrue(results.any { it.id == GameId.MemoryMatch })
    }

    @Test fun searchMatchesCategory() {
        val results = GameRegistry.search("brain")
        assertTrue(results.any { it.id == GameId.TileMerge })
        assertTrue(results.any { it.id == GameId.Minesweeper })
    }

    @Test fun searchNoMatchReturnsEmpty() {
        assertTrue(GameRegistry.search("zzzzz").isEmpty())
    }

    // --- recents helper ---

    @Test fun recentsPutsMostRecentFirst() {
        val next = ScoreLogic.updatedRecents(listOf("A", "B"), "C")
        assertEquals(listOf("C", "A", "B"), next)
    }

    @Test fun recentsDeduplicatesReplays() {
        val next = ScoreLogic.updatedRecents(listOf("A", "B", "C"), "B")
        assertEquals(listOf("B", "A", "C"), next)
    }

    @Test fun recentsCapsAtMax() {
        val next = ScoreLogic.updatedRecents(listOf("A", "B", "C", "D", "E"), "F", max = 5)
        assertEquals(listOf("F", "A", "B", "C", "D"), next)
    }

    // --- new score helpers ---

    @Test fun betterMovesWhenNoBestYet() = assertTrue(ScoreLogic.isBetterMoves(30, 0))
    @Test fun fewerMovesIsBetter() = assertTrue(ScoreLogic.isBetterMoves(10, 20))
    @Test fun moreMovesIsNotBetter() = assertTrue(!ScoreLogic.isBetterMoves(25, 20))
    @Test fun fasterReactionIsBetter() = assertTrue(ScoreLogic.isBetterReaction(200L, 300L))
    @Test fun anyReactionBeatsZero() = assertTrue(ScoreLogic.isBetterReaction(900L, 0L))

    @Test fun levelProgression() {
        assertEquals(1, ScoreLogic.levelForXp(0))
        assertEquals(1, ScoreLogic.levelForXp(99))
        assertEquals(2, ScoreLogic.levelForXp(100))
        assertEquals(3, ScoreLogic.levelForXp(250))
    }
}
