package net.sclan.minigames

import net.sclan.minigames.ui.LADDER_WORDS
import net.sclan.minigames.ui.findLadder
import net.sclan.minigames.ui.generateLadderPuzzle
import net.sclan.minigames.ui.ladderNeighbors
import net.sclan.minigames.ui.oneLetterApart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WordLadderTest {

    @Test fun allWordsAreFourLetters() {
        LADDER_WORDS.forEach { assertEquals(4, it.length) }
    }

    @Test fun oneLetterApartDetection() {
        assertTrue(oneLetterApart("COLD", "CORD"))
        assertFalse(oneLetterApart("COLD", "WARM"))
        assertFalse(oneLetterApart("COLD", "COLD"))
    }

    @Test fun neighborsDifferByOneAndAreInDict() {
        val n = ladderNeighbors("CORD", LADDER_WORDS)
        assertTrue(n.isNotEmpty())
        n.forEach {
            assertTrue(oneLetterApart("CORD", it))
            assertTrue(it in LADDER_WORDS)
        }
    }

    @Test fun classicLadderExists() {
        val ladder = findLadder("COLD", "WARM", LADDER_WORDS)
        assertNotNull(ladder)
        assertEquals("COLD", ladder!!.first())
        assertEquals("WARM", ladder.last())
        // Consecutive rungs differ by exactly one letter and are all valid words
        ladder.zipWithNext().forEach { (a, b) ->
            assertTrue(oneLetterApart(a, b))
        }
        ladder.forEach { assertTrue(it in LADDER_WORDS) }
    }

    @Test fun unreachableReturnsNull() {
        // "ZZZZ" is not in the dictionary graph
        assertNull(findLadder("COLD", "ZZZZ", LADDER_WORDS))
    }

    @Test fun generatedPuzzlesAreSolvable() {
        repeat(20) { seed ->
            val (start, end) = generateLadderPuzzle(LADDER_WORDS, random = Random(seed))
            assertTrue(start in LADDER_WORDS)
            assertTrue(end in LADDER_WORDS)
            val ladder = findLadder(start, end, LADDER_WORDS)
            assertNotNull("no ladder for $start -> $end", ladder)
            assertTrue(ladder!!.size in 4..6)
        }
    }
}
