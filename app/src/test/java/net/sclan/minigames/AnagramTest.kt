package net.sclan.minigames

import net.sclan.minigames.ui.AnagramLength
import net.sclan.minigames.ui.anagramWordPool
import net.sclan.minigames.ui.scrambleWord
import net.sclan.minigames.ui.tilesFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AnagramTest {

    @Test fun poolRespectsLengthBounds() {
        AnagramLength.entries.forEach { mode ->
            val pool = anagramWordPool(mode.minLen, mode.maxLen)
            assertTrue("pool for ${mode.name} must not be empty", pool.size >= mode.rounds)
            pool.forEach { assertTrue(it.length in mode.minLen..mode.maxLen) }
        }
    }

    @Test fun scramblePreservesLetters() {
        repeat(10) { seed ->
            val scrambled = scrambleWord("ARCADE", Random(seed))
            assertEquals("ARCADE".toList().sorted(), scrambled.toList().sorted())
        }
    }

    @Test fun scrambleUsuallyDiffersFromOriginal() {
        val scrambled = scrambleWord("PUZZLE", Random(1))
        assertNotEquals("PUZZLE", scrambled)
    }

    @Test fun singleRepeatedLetterWordUnchanged() {
        assertEquals("AA", scrambleWord("AA", Random(1)))
    }

    @Test fun tilesKeepOrderAndIds() {
        val tiles = tilesFor("WORD")
        assertEquals(4, tiles.size)
        assertEquals(listOf(0, 1, 2, 3), tiles.map { it.id })
        assertEquals("WORD", tiles.joinToString("") { it.letter.toString() })
    }
}
