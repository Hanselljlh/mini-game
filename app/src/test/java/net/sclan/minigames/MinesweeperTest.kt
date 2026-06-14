package net.sclan.minigames

import net.sclan.minigames.ui.MS_COLS
import net.sclan.minigames.ui.MS_MINES
import net.sclan.minigames.ui.MS_ROWS
import net.sclan.minigames.ui.MsCell
import net.sclan.minigames.ui.checkWinMs
import net.sclan.minigames.ui.createMsBoard
import net.sclan.minigames.ui.revealMs
import net.sclan.minigames.ui.toggleFlagMs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MinesweeperTest {

    // --- createMsBoard ---

    @Test fun boardHasCorrectDimensions() {
        val board = createMsBoard()
        assertEquals(MS_ROWS, board.size)
        board.forEach { assertEquals(MS_COLS, it.size) }
    }

    @Test fun boardHasExactlyTenMines() {
        val board = createMsBoard()
        val mineCount = board.sumOf { row -> row.count { it.isMine } }
        assertEquals(MS_MINES, mineCount)
    }

    @Test fun avoidCellIsNeverAMine() {
        repeat(50) {
            val board = createMsBoard(avoidRow = 4, avoidCol = 4)
            assertFalse("Cell (4,4) must be safe", board[4][4].isMine)
        }
    }

    @Test fun neighborCountsAreCorrect() {
        val board = createMsBoard()
        for (r in 0 until MS_ROWS) for (c in 0 until MS_COLS) {
            if (board[r][c].isMine) continue
            val expected = (-1..1).sumOf { dr ->
                (-1..1).count { dc ->
                    val nr = r + dr; val nc = c + dc
                    nr in 0 until MS_ROWS && nc in 0 until MS_COLS && board[nr][nc].isMine
                }
            }
            assertEquals("Neighbor count at ($r,$c)", expected, board[r][c].neighborMines)
        }
    }

    // --- revealMs ---

    @Test fun revealingSafeNumberedCellOnlyRevealsThatCell() {
        // Build a tiny controlled board: all safe, cell (0,0) has neighborMines=1
        val board = List(MS_ROWS) { r ->
            List(MS_COLS) { c ->
                when {
                    r == 0 && c == 1 -> MsCell(isMine = true)
                    r == 0 && c == 0 -> MsCell(neighborMines = 1)
                    else -> MsCell(neighborMines = 0)
                }
            }
        }
        val result = revealMs(board, 0, 0)
        assertTrue(result[0][0].isRevealed)
        // Adjacent numbered cell should NOT auto-expand
        assertFalse("Mine cell must not be auto-revealed", result[0][1].isRevealed)
    }

    @Test fun revealingZeroCellFloodFills() {
        // All-zero board (no mines, no numbers) — revealing one cell should reveal all
        val board = List(MS_ROWS) { List(MS_COLS) { MsCell(neighborMines = 0) } }
        val result = revealMs(board, 0, 0)
        val revealed = result.sumOf { row -> row.count { it.isRevealed } }
        assertEquals(MS_ROWS * MS_COLS, revealed)
    }

    @Test fun revealFlaggedCellDoesNothing() {
        val board = List(MS_ROWS) { List(MS_COLS) { MsCell(neighborMines = 0) } }
        val flagged = board.mapIndexed { r, row ->
            row.mapIndexed { c, cell -> if (r == 0 && c == 0) cell.copy(isFlagged = true) else cell }
        }
        val result = revealMs(flagged, 0, 0)
        assertFalse(result[0][0].isRevealed)
    }

    // --- toggleFlagMs ---

    @Test fun flaggingUnrevealedCellSetsFlag() {
        val board = List(MS_ROWS) { List(MS_COLS) { MsCell() } }
        val result = toggleFlagMs(board, 2, 3)
        assertTrue(result[2][3].isFlagged)
    }

    @Test fun flaggingFlaggedCellClearsFlag() {
        val board = List(MS_ROWS) { List(MS_COLS) { MsCell() } }
        val flagged = toggleFlagMs(board, 2, 3)
        val cleared = toggleFlagMs(flagged, 2, 3)
        assertFalse(cleared[2][3].isFlagged)
    }

    @Test fun flaggingRevealedCellDoesNothing() {
        val board = List(MS_ROWS) { r ->
            List(MS_COLS) { c -> if (r == 0 && c == 0) MsCell(isRevealed = true) else MsCell() }
        }
        val result = toggleFlagMs(board, 0, 0)
        assertFalse(result[0][0].isFlagged)
    }

    // --- checkWinMs ---

    @Test fun winWhenAllNonMinesRevealed() {
        val board = List(MS_ROWS) { r ->
            List(MS_COLS) { c ->
                if (r == 0 && c == 0) MsCell(isMine = true)
                else MsCell(isRevealed = true)
            }
        }
        assertTrue(checkWinMs(board))
    }

    @Test fun noWinWhenSomeNonMinesHidden() {
        val board = List(MS_ROWS) { List(MS_COLS) { MsCell() } }
        assertFalse(checkWinMs(board))
    }
}
