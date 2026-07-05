package net.sclan.minigames

import net.sclan.minigames.ui.C4_COLS
import net.sclan.minigames.ui.C4_ROWS
import net.sclan.minigames.ui.dropC4
import net.sclan.minigames.ui.easyBotC4
import net.sclan.minigames.ui.emptyC4Board
import net.sclan.minigames.ui.isFullC4
import net.sclan.minigames.ui.smartBotC4
import net.sclan.minigames.ui.validColsC4
import net.sclan.minigames.ui.winnerC4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FourInARowTest {

    @Test fun dropLandsAtBottom() {
        val board = dropC4(emptyC4Board(), 3, 1)!!
        assertEquals(1, board[C4_ROWS - 1][3])
        assertEquals(0, board[C4_ROWS - 2][3])
    }

    @Test fun dropsStack() {
        var board = emptyC4Board()
        board = dropC4(board, 3, 1)!!
        board = dropC4(board, 3, 2)!!
        assertEquals(1, board[C4_ROWS - 1][3])
        assertEquals(2, board[C4_ROWS - 2][3])
    }

    @Test fun fullColumnRejectsDrop() {
        var board = emptyC4Board()
        repeat(C4_ROWS) { board = dropC4(board, 0, 1)!! }
        assertNull(dropC4(board, 0, 2))
        assertTrue(0 !in validColsC4(board))
    }

    @Test fun horizontalWin() {
        var board = emptyC4Board()
        for (c in 0..3) board = dropC4(board, c, 1)!!
        assertEquals(1, winnerC4(board))
    }

    @Test fun verticalWin() {
        var board = emptyC4Board()
        repeat(4) { board = dropC4(board, 5, 2)!! }
        assertEquals(2, winnerC4(board))
    }

    @Test fun diagonalWin() {
        var board = emptyC4Board()
        // Build a staircase for player 1 on columns 0..3
        board = dropC4(board, 0, 1)!!
        board = dropC4(board, 1, 2)!!
        board = dropC4(board, 1, 1)!!
        board = dropC4(board, 2, 2)!!
        board = dropC4(board, 2, 2)!!
        board = dropC4(board, 2, 1)!!
        board = dropC4(board, 3, 2)!!
        board = dropC4(board, 3, 2)!!
        board = dropC4(board, 3, 2)!!
        board = dropC4(board, 3, 1)!!
        assertEquals(1, winnerC4(board))
    }

    @Test fun noWinnerOnEmptyBoard() {
        assertEquals(0, winnerC4(emptyC4Board()))
        assertTrue(!isFullC4(emptyC4Board()))
    }

    @Test fun smartBotTakesWinningMove() {
        var board = emptyC4Board()
        repeat(3) { board = dropC4(board, 6, 2)!! } // bot has 3 in column 6
        assertEquals(6, smartBotC4(board))
    }

    @Test fun smartBotBlocksOpponent() {
        var board = emptyC4Board()
        for (c in 0..2) board = dropC4(board, c, 1)!! // human threatens 0-3 across
        assertEquals(3, smartBotC4(board))
    }

    @Test fun easyBotPlaysValidColumn() {
        val col = easyBotC4(emptyC4Board())
        assertNotNull(col)
        assertTrue(col!! in 0 until C4_COLS)
    }
}
