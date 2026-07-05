package net.sclan.minigames

import net.sclan.minigames.ui.WordSearchDifficulty
import net.sclan.minigames.ui.generateWordSearch
import net.sclan.minigames.ui.lineBetween
import net.sclan.minigames.ui.matchWord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WordSearchTest {

    @Test fun generatorPlacesRequestedWords() {
        repeat(10) { seed ->
            WordSearchDifficulty.entries.forEach { d ->
                val puzzle = generateWordSearch(d.gridSize, d.wordCount, Random(seed))
                assertTrue(
                    "wanted ${d.wordCount}, got ${puzzle.words.size}",
                    puzzle.words.size == d.wordCount
                )
            }
        }
    }

    @Test fun placedWordsReadCorrectlyFromGrid() {
        val puzzle = generateWordSearch(10, 7, Random(42))
        puzzle.words.forEach { pw ->
            val readBack = pw.cells.map { (r, c) -> puzzle.grid[r][c] }.joinToString("")
            assertEquals(pw.word, readBack)
        }
    }

    @Test fun gridFullyFilled() {
        val puzzle = generateWordSearch(8, 5, Random(7))
        puzzle.grid.forEach { row -> row.forEach { ch -> assertTrue(ch in 'A'..'Z') } }
    }

    @Test fun lineBetweenHorizontal() {
        assertEquals(listOf(2 to 1, 2 to 2, 2 to 3), lineBetween(2 to 1, 2 to 3))
    }

    @Test fun lineBetweenDiagonal() {
        assertEquals(listOf(0 to 0, 1 to 1, 2 to 2), lineBetween(0 to 0, 2 to 2))
    }

    @Test fun lineBetweenReversed() {
        assertEquals(listOf(2 to 3, 2 to 2, 2 to 1), lineBetween(2 to 3, 2 to 1))
    }

    @Test fun invalidLineIsNull() {
        assertNull(lineBetween(0 to 0, 1 to 2)) // knight move: not straight
    }

    @Test fun matchWordFindsForwardAndBackward() {
        val puzzle = generateWordSearch(10, 5, Random(3))
        val target = puzzle.words.first()
        assertNotNull(matchWord(puzzle, target.cells, emptySet()))
        assertNotNull(matchWord(puzzle, target.cells.reversed(), emptySet()))
        assertNull(matchWord(puzzle, target.cells, setOf(target.word))) // already found
    }
}
