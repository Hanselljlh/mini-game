package net.sclan.minigames

import net.sclan.minigames.ui.SortState
import net.sclan.minigames.ui.canPour
import net.sclan.minigames.ui.generateSortPuzzle
import net.sclan.minigames.ui.pour
import net.sclan.minigames.ui.sortSolved
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SortPuzzleTest {

    @Test fun generatorDealsAllUnits() {
        val s = generateSortPuzzle(colors = 6, random = Random(1))
        assertEquals(8, s.containers.size) // 6 filled + 2 empty
        assertEquals(24, s.containers.sumOf { it.size })
        (0 until 6).forEach { color ->
            assertEquals(4, s.containers.sumOf { c -> c.count { it == color } })
        }
        assertTrue(s.containers.takeLast(2).all { it.isEmpty() })
    }

    @Test fun cannotPourOntoDifferentColor() {
        val s = SortState(listOf(listOf(0, 0), listOf(1, 1), emptyList()))
        assertFalse(canPour(s, 0, 1))
        assertNull(pour(s, 0, 1))
    }

    @Test fun pourOntoEmptyMovesWholeRun() {
        val s = SortState(listOf(listOf(1, 0, 0), emptyList()))
        val next = pour(s, 0, 1)!!
        assertEquals(listOf(1), next.containers[0])
        assertEquals(listOf(0, 0), next.containers[1])
    }

    @Test fun pourLimitedByCapacity() {
        val s = SortState(listOf(listOf(0, 0, 0), listOf(1, 1, 1, 0)), capacity = 4)
        // destination has 0 on top but no space for all three; only... it has 4/4 → no pour
        assertFalse(canPour(s, 0, 1))
        val s2 = SortState(listOf(listOf(0, 0, 0), listOf(1, 1, 0)), capacity = 4)
        val next = pour(s2, 0, 1)!!
        assertEquals(listOf(0, 0), next.containers[0]) // one unit moved (dst had 1 slot)
        assertEquals(listOf(1, 1, 0, 0), next.containers[1])
    }

    @Test fun cannotPourFromEmptyOrToSelf() {
        val s = SortState(listOf(emptyList(), listOf(0)))
        assertFalse(canPour(s, 0, 1))
        assertFalse(canPour(s, 1, 1))
    }

    @Test fun solvedDetection() {
        assertTrue(sortSolved(SortState(listOf(listOf(0, 0, 0, 0), listOf(1, 1, 1, 1), emptyList()))))
        assertFalse(sortSolved(SortState(listOf(listOf(0, 0, 0, 1), listOf(1, 1, 1, 0)))))
        assertFalse(sortSolved(SortState(listOf(listOf(0, 0), listOf(1, 1))))) // not full
    }
}
