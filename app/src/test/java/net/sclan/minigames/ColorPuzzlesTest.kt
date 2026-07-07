package net.sclan.minigames

import net.sclan.minigames.ui.blockGroup
import net.sclan.minigames.ui.blockScoreFor
import net.sclan.minigames.ui.blocksRemaining
import net.sclan.minigames.ui.floodComplete
import net.sclan.minigames.ui.floodFill
import net.sclan.minigames.ui.generateBlockGrid
import net.sclan.minigames.ui.generateFloodGrid
import net.sclan.minigames.ui.hasBlockMoves
import net.sclan.minigames.ui.removeBlockGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ColorPuzzlesTest {

    // --- Multi-Color Fill ---

    @Test fun floodFillAbsorbsConnectedRegion() {
        val grid = listOf(
            listOf(0, 0, 1),
            listOf(0, 1, 1),
            listOf(2, 1, 0)
        )
        val next = floodFill(grid, 1)
        assertEquals(1, next[0][0])
        assertEquals(1, next[0][1])
        assertEquals(1, next[1][0])
        // Now the whole top-left region plus old 1s form one region of 1s
        assertEquals(1, next[0][2])
        assertEquals(2, next[2][0]) // untouched
    }

    @Test fun floodSameColorIsNoop() {
        val grid = generateFloodGrid(8, 4, Random(1))
        assertEquals(grid, floodFill(grid, grid[0][0]))
    }

    @Test fun floodCompleteDetection() {
        assertTrue(floodComplete(List(3) { List(3) { 2 } }))
        assertFalse(floodComplete(listOf(listOf(1, 1), listOf(1, 0))))
    }

    // --- Color Blocks ---

    @Test fun groupFindsConnectedSameColor() {
        val grid = listOf(
            listOf(0, 0, 1),
            listOf(0, 1, 1),
            listOf(2, 2, 1)
        )
        assertEquals(3, blockGroup(grid, 0, 0).size)
        assertEquals(4, blockGroup(grid, 0, 2).size)
        assertEquals(2, blockGroup(grid, 2, 0).size)
    }

    @Test fun removeAppliesGravityAndColumnCollapse() {
        val grid = listOf(
            listOf(0, 1, 2),
            listOf(0, 1, 2),
            listOf(0, 1, 2)
        )
        // Remove the whole middle column (three 1s)
        val next = removeBlockGroup(grid, blockGroup(grid, 0, 1))
        // Column of 2s should slide left into column index 1
        assertEquals(listOf(0, 2, -1), next[0])
        assertEquals(listOf(0, 2, -1), next[1])
        assertEquals(listOf(0, 2, -1), next[2])
        assertEquals(6, blocksRemaining(next))
    }

    @Test fun gravityDropsFloatingBlocks() {
        val grid = listOf(
            listOf(3, -1),
            listOf(1, -1),
            listOf(1, -1)
        )
        val next = removeBlockGroup(grid, blockGroup(grid, 1, 0))
        assertEquals(-1, next[0][0])
        assertEquals(-1, next[1][0])
        assertEquals(3, next[2][0]) // the 3 fell to the bottom
    }

    @Test fun scoreIsQuadratic() {
        assertEquals(1, blockScoreFor(2))
        assertEquals(81, blockScoreFor(10))
    }

    @Test fun movesDetection() {
        assertTrue(hasBlockMoves(listOf(listOf(0, 0), listOf(1, 2))))
        assertFalse(hasBlockMoves(listOf(listOf(0, 1), listOf(1, 0))))
        assertFalse(hasBlockMoves(listOf(listOf(-1, -1), listOf(-1, -1))))
    }

    @Test fun generatorRespectsDimensions() {
        val grid = generateBlockGrid(10, 9, 5, Random(2))
        assertEquals(10, grid.size)
        assertEquals(9, grid[0].size)
        assertTrue(grid.all { row -> row.all { it in 0 until 5 } })
    }
}
