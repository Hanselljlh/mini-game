package net.sclan.minigames

import net.sclan.minigames.ui.LUDO_DONE
import net.sclan.minigames.ui.LUDO_SAFE
import net.sclan.minigames.ui.LUDO_START
import net.sclan.minigames.ui.LUDO_TRACK
import net.sclan.minigames.ui.ludoAbsCell
import net.sclan.minigames.ui.ludoApply
import net.sclan.minigames.ui.ludoBotPick
import net.sclan.minigames.ui.ludoLegalTokens
import net.sclan.minigames.ui.newLudo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LudoTest {

    @Test fun trackHas52DistinctCells() {
        assertEquals(52, LUDO_TRACK.size)
        assertEquals(52, LUDO_TRACK.toSet().size)
    }

    @Test fun startCellsMatchClassicBoard() {
        assertEquals(1 to 6, LUDO_TRACK[LUDO_START[0]])
        assertEquals(8 to 1, LUDO_TRACK[LUDO_START[1]])
        assertEquals(13 to 8, LUDO_TRACK[LUDO_START[2]])
        assertEquals(6 to 13, LUDO_TRACK[LUDO_START[3]])
    }

    @Test fun needSixToLeaveYard() {
        val s = newLudo(2)
        assertTrue(ludoLegalTokens(s, 5).isEmpty())
        assertEquals(listOf(0, 1, 2, 3), ludoLegalTokens(s, 6))
    }

    @Test fun sixGrantsExtraTurn() {
        val s = newLudo(2)
        val after = ludoApply(s, 0, 6)
        assertEquals(0, after.turn) // same player again
        assertEquals(0, after.tokens[0][0]) // entered at rel 0
    }

    @Test fun nonSixPassesTurn() {
        var s = newLudo(2)
        s = ludoApply(s, 0, 6) // enter
        s = ludoApply(s, 0, 3) // move 3
        assertEquals(1, s.turn)
        assertEquals(3, s.tokens[0][0])
    }

    @Test fun captureSendsOpponentHome() {
        // P0 token at rel 20 (abs 20); P1 token also on abs 20: rel = (20 - 13) mod 52 = 7
        var s = newLudo(2).copy(
            tokens = listOf(listOf(17, -1, -1, -1), listOf(7, -1, -1, -1)),
            turn = 0
        )
        assertTrue(20 !in LUDO_SAFE)
        assertEquals(20, ludoAbsCell(1, 7))
        s = ludoApply(s, 0, 3) // 17 + 3 = rel 20 → abs 20 → capture
        assertEquals(20, s.tokens[0][0])
        assertEquals(-1, s.tokens[1][0])
        assertEquals("Captured!", s.message)
    }

    @Test fun safeCellPreventsCapture() {
        // abs 21 is a safe star. P1 rel = (21-13)=8 → abs 21; P0 moves onto it.
        var s = newLudo(2).copy(
            tokens = listOf(listOf(18, -1, -1, -1), listOf(8, -1, -1, -1)),
            turn = 0
        )
        assertTrue(21 in LUDO_SAFE)
        s = ludoApply(s, 0, 3)
        assertEquals(21, s.tokens[0][0])
        assertEquals(8, s.tokens[1][0]) // survived
    }

    @Test fun exactRollRequiredToFinish() {
        val s = newLudo(2).copy(tokens = listOf(listOf(54, -1, -1, -1), List(4) { -1 }))
        assertTrue(ludoLegalTokens(s, 3).isEmpty()) // 54+3 = 57 > 56, and others need a 6
        assertEquals(listOf(0), ludoLegalTokens(s, 2)) // 54+2 = 56 exactly
        val done = ludoApply(s, 0, 2)
        assertEquals(LUDO_DONE, done.tokens[0][0])
    }

    @Test fun winnerDetected() {
        val s = newLudo(2).copy(tokens = listOf(List(4) { LUDO_DONE }, List(4) { -1 }))
        assertEquals(0, s.winner)
    }

    @Test fun botPrefersFinishingMove() {
        val s = newLudo(2).copy(
            tokens = listOf(List(4) { -1 }, listOf(54, 10, -1, -1)),
            turn = 1
        )
        assertEquals(0, ludoBotPick(s, 2)) // 54+2 finishes
    }

    @Test fun botReturnsNullWithNoMoves() {
        val s = newLudo(2).copy(turn = 1) // all in yard, dice 3
        assertNull(ludoBotPick(s, 3))
    }

    @Test fun homeColumnIsPrivate() {
        // A token in the home column (rel 52) can't be captured: opponent can't even reach it.
        var s = newLudo(2).copy(
            tokens = listOf(listOf(52, -1, -1, -1), listOf(35, -1, -1, -1)),
            turn = 1
        )
        s = ludoApply(s, 0, 4) // P1 moves rel 35→39 (abs (13+39)%52 = 0? that's P0 start, safe anyway)
        assertEquals(52, s.tokens[0][0]) // untouched
    }
}
