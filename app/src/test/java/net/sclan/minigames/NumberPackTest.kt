package net.sclan.minigames

import net.sclan.minigames.ui.CrossMathDifficulty
import net.sclan.minigames.ui.NumberConnectDifficulty
import net.sclan.minigames.ui.crossApply
import net.sclan.minigames.ui.crossMathSolved
import net.sclan.minigames.ui.generateCrossMath
import net.sclan.minigames.ui.generateNumberPath
import net.sclan.minigames.ui.mcApplyMerge
import net.sclan.minigames.ui.mcCanExtend
import net.sclan.minigames.ui.mcHasAnyPair
import net.sclan.minigames.ui.mcMergeValue
import net.sclan.minigames.ui.mcNewBoard
import net.sclan.minigames.ui.revealedSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class NumberPackTest {

    // --- Merge Chain ---

    @Test fun mergeValueIsNextPowerOfTwo() {
        assertEquals(4, mcMergeValue(4))
        assertEquals(8, mcMergeValue(5))
        assertEquals(8, mcMergeValue(8))
        assertEquals(16, mcMergeValue(9))
        assertEquals(64, mcMergeValue(48))
    }

    @Test fun chainMustStartWithEqualPair() {
        val board = listOf(
            listOf(2, 4, 2, 2, 2),
            listOf(2, 2, 2, 2, 2),
            listOf(2, 2, 2, 2, 2),
            listOf(2, 2, 2, 2, 2),
            listOf(2, 2, 2, 2, 2),
            listOf(2, 2, 2, 2, 2)
        )
        assertFalse(mcCanExtend(board, listOf(0 to 0), 0 to 1)) // 2 → 4 not allowed as second
        assertTrue(mcCanExtend(board, listOf(0 to 0), 1 to 0))  // 2 → 2 fine
        assertTrue(mcCanExtend(board, listOf(0 to 0, 1 to 0), 0 to 1)) // then 4 = double allowed
    }

    @Test fun chainRejectsNonAdjacentAndRevisits() {
        val board = mcNewBoard(Random(1))
        assertFalse(mcCanExtend(board, listOf(0 to 0), 3 to 3))
        assertFalse(mcCanExtend(board, listOf(0 to 0, 0 to 1), 0 to 0))
    }

    @Test fun mergeKeepsBoardFullAndScores() {
        val board = List(6) { List(5) { 2 } }
        val chain = listOf(5 to 0, 5 to 1, 5 to 2)
        val (next, result) = mcApplyMerge(board, chain, Random(3))
        assertEquals(8, result) // sum 6 → next pow2 = 8
        assertEquals(6, next.size)
        assertTrue(next.all { row -> row.size == 5 && row.all { it > 0 } })
        assertEquals(8, next[5][2]) // result lands on last cell, bottom row unaffected by gravity
    }

    @Test fun fullEqualBoardHasPairs() {
        assertTrue(mcHasAnyPair(List(6) { List(5) { 2 } }))
    }

    // --- Cross Math ---

    @Test fun generatedPuzzlesAreSelfConsistent() {
        CrossMathDifficulty.entries.forEach { d ->
            repeat(20) { seed ->
                val p = generateCrossMath(d.ops, d.decoys, Random(seed))
                assertEquals(p.r1, crossApply(p.a, p.op1, p.b))
                assertEquals(p.r2, crossApply(p.c, p.op2, p.d))
                assertEquals(p.r3, crossApply(p.a, p.op3, p.c))
                assertEquals(p.r4, crossApply(p.b, p.op4, p.d))
                assertTrue(crossMathSolved(p, p.a, p.b, p.c, p.d))
                assertEquals(4 + d.decoys, p.tray.size)
                assertTrue(p.tray.containsAll(listOf(p.a, p.b, p.c, p.d)))
            }
        }
    }

    @Test fun wrongAssignmentDoesNotSolve() {
        val p = generateCrossMath(listOf('+'), 0, Random(5))
        // Swapping a and b keeps r1 (addition commutes) but breaks the columns
        // unless the puzzle happens to be symmetric — use clearly wrong values instead.
        assertFalse(crossMathSolved(p, null, p.b, p.c, p.d))
    }

    // --- Number Connect ---

    @Test fun pathVisitsEveryCellExactlyOnce() {
        NumberConnectDifficulty.entries.forEach { d ->
            repeat(5) { seed ->
                val path = generateNumberPath(d.gridSize, Random(seed))
                assertEquals(d.gridSize * d.gridSize, path.size)
                assertEquals(path.size, path.toSet().size)
                path.forEach { (r, c) ->
                    assertTrue(r in 0 until d.gridSize && c in 0 until d.gridSize)
                }
            }
        }
    }

    @Test fun pathStepsAreOrthogonallyAdjacent() {
        val path = generateNumberPath(6, Random(8))
        path.zipWithNext().forEach { (a, b) ->
            val dist = kotlin.math.abs(a.first - b.first) + kotlin.math.abs(a.second - b.second)
            assertEquals(1, dist)
        }
    }

    @Test fun revealedStepsIncludeEndpoints() {
        val revealed = revealedSteps(36, 4)
        assertTrue(1 in revealed)
        assertTrue(36 in revealed)
        assertTrue(4 in revealed)
        assertFalse(3 in revealed)
    }
}
