package net.sclan.minigames

import net.sclan.minigames.ui.winner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TicTacToeTest {

    private fun board(vararg cells: String) = cells.toList()

    @Test fun emptyBoardHasNoWinner() {
        assertNull(winner(board("","","","","","","","","")))
    }

    @Test fun firstRowWin() {
        assertEquals("X", winner(board("X","X","X","","","","","","")))
    }

    @Test fun columnWin() {
        assertEquals("O", winner(board("O","","","O","","","O","","")))
    }

    @Test fun diagonalWin() {
        assertEquals("X", winner(board("X","O","O","","X","","","","X")))
    }

    @Test fun antiDiagonalWin() {
        assertEquals("O", winner(board("","","O","","O","","O","","")))
    }

    @Test fun drawHasNoWinner() {
        assertNull(winner(board("X","O","X","X","X","O","O","X","O")))
    }

    @Test fun partialBoardNoWinner() {
        assertNull(winner(board("X","","O","","X","","","","O")))
    }
}
