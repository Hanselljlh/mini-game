package net.sclan.minigames

import net.sclan.minigames.ui.Board2048
import net.sclan.minigames.ui.Dir2048
import net.sclan.minigames.ui.applyMove2048
import net.sclan.minigames.ui.emptyBoard2048
import net.sclan.minigames.ui.hasValidMoves2048
import net.sclan.minigames.ui.slideLeft
import net.sclan.minigames.ui.transpose2048
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Game2048Test {

    // --- slideLeft ---

    @Test fun slideLeftMergesAdjacentPair() {
        val (row, score) = slideLeft(listOf(2, 2, 0, 0))
        assertEquals(listOf(4, 0, 0, 0), row)
        assertEquals(4, score)
    }

    @Test fun slideLeftDoesNotDoubleMerge() {
        // [2,2,2,2] → [4,4,0,0], score 8
        val (row, score) = slideLeft(listOf(2, 2, 2, 2))
        assertEquals(listOf(4, 4, 0, 0), row)
        assertEquals(8, score)
    }

    @Test fun slideLeftPushesNonZeroLeft() {
        val (row, score) = slideLeft(listOf(0, 0, 4, 0))
        assertEquals(listOf(4, 0, 0, 0), row)
        assertEquals(0, score)
    }

    @Test fun slideLeftMergesAcrossGap() {
        val (row, score) = slideLeft(listOf(2, 0, 2, 4))
        assertEquals(listOf(4, 4, 0, 0), row)
        assertEquals(4, score)
    }

    @Test fun slideLeftNoMergeWhenDifferent() {
        val (row, score) = slideLeft(listOf(2, 4, 2, 4))
        assertEquals(listOf(2, 4, 2, 4), row)
        assertEquals(0, score)
    }

    // --- transpose2048 ---

    @Test fun transposeSwapsRowsAndCols() {
        val board: Board2048 = listOf(
            listOf(1, 2, 3, 4),
            listOf(5, 6, 7, 8),
            listOf(9, 10, 11, 12),
            listOf(13, 14, 15, 16)
        )
        val t = transpose2048(board)
        assertEquals(1, t[0][0])
        assertEquals(5, t[0][1])
        assertEquals(2, t[1][0])
        assertEquals(board, transpose2048(t)) // double-transpose = identity
    }

    // --- applyMove2048 ---

    @Test fun moveLeftSlidesAllRows() {
        val board: Board2048 = listOf(
            listOf(0, 2, 0, 2),
            listOf(4, 0, 0, 4),
            listOf(0, 0, 0, 0),
            listOf(2, 2, 2, 2)
        )
        val (result, score) = applyMove2048(Dir2048.Left, board)
        assertEquals(listOf(4, 0, 0, 0), result[0])
        assertEquals(listOf(8, 0, 0, 0), result[1])
        assertEquals(listOf(0, 0, 0, 0), result[2])
        assertEquals(listOf(4, 4, 0, 0), result[3])
        assertEquals(4 + 8 + 8, score)
    }

    @Test fun moveRightIsOppositeOfLeft() {
        val board: Board2048 = listOf(
            listOf(2, 2, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        val (result, _) = applyMove2048(Dir2048.Right, board)
        assertEquals(listOf(0, 0, 0, 4), result[0])
    }

    @Test fun moveUpSlidesColumns() {
        val board: Board2048 = listOf(
            listOf(0, 0, 0, 0),
            listOf(2, 0, 0, 0),
            listOf(2, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        val (result, score) = applyMove2048(Dir2048.Up, board)
        assertEquals(4, result[0][0])
        assertEquals(0, result[1][0])
        assertEquals(4, score)
    }

    @Test fun moveDownSlidesColumnsDown() {
        val board: Board2048 = listOf(
            listOf(0, 0, 0, 0),
            listOf(2, 0, 0, 0),
            listOf(2, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        val (result, score) = applyMove2048(Dir2048.Down, board)
        assertEquals(4, result[3][0])
        assertEquals(0, result[2][0])
        assertEquals(4, score)
    }

    @Test fun unchangedBoardDetected() {
        val board: Board2048 = listOf(
            listOf(2, 4, 8, 16),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        // Moving left on an already-left-packed row changes nothing
        val (result, _) = applyMove2048(Dir2048.Left, board)
        assertEquals(board, result)
    }

    // --- hasValidMoves2048 ---

    @Test fun emptyBoardHasMoves() {
        assertTrue(hasValidMoves2048(emptyBoard2048()))
    }

    @Test fun fullBoardWithMergesHasMoves() {
        val board: Board2048 = listOf(
            listOf(2, 4, 8, 16),
            listOf(4, 2, 4, 8),
            listOf(8, 4, 2, 4),
            listOf(16, 8, 4, 2)
        )
        // No zeros and no adjacent equals — no valid moves
        assertFalse(hasValidMoves2048(board))
    }

    @Test fun boardWithOneZeroHasMoves() {
        val board: Board2048 = listOf(
            listOf(2, 4, 8, 16),
            listOf(4, 2, 4, 8),
            listOf(8, 4, 2, 4),
            listOf(16, 8, 4, 0)
        )
        assertTrue(hasValidMoves2048(board))
    }
}
