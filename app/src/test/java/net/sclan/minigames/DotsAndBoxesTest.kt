package net.sclan.minigames

import net.sclan.minigames.ui.DotsState
import net.sclan.minigames.ui.claimEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DotsAndBoxesTest {

    @Test fun claimingEdgePassesTurn() {
        val s0 = DotsState(n = 2)
        val s1 = claimEdge(s0, horizontal = true, row = 0, col = 0)!!
        assertEquals(2, s1.turn)
        assertEquals(1, s1.hEdges[0 to 0])
    }

    @Test fun claimingTakenEdgeReturnsNull() {
        val s0 = DotsState(n = 2)
        val s1 = claimEdge(s0, true, 0, 0)!!
        assertNull(claimEdge(s1, true, 0, 0))
    }

    @Test fun completingBoxScoresAndKeepsTurn() {
        var s = DotsState(n = 2)
        // Three sides of box (0,0) drawn alternately; player 1 completes it.
        s = claimEdge(s, true, 0, 0)!!   // p1 top      → p2's turn
        s = claimEdge(s, true, 1, 0)!!   // p2 bottom   → p1's turn
        s = claimEdge(s, false, 0, 0)!!  // p1 left     → p2's turn
        assertEquals(2, s.turn)
        s = claimEdge(s, false, 0, 1)!!  // p2 right — completes the box
        assertEquals(1, s.boxes.size)
        assertEquals(2, s.boxes[0 to 0])
        assertEquals(2, s.turn) // completer moves again
        assertEquals(1, s.scoreOf2)
        assertEquals(0, s.scoreOf1)
    }

    @Test fun oneEdgeCanCompleteTwoBoxes() {
        var s = DotsState(n = 2)
        // Surround both boxes in row 0 except the shared middle vertical edge...
        // Actually surround boxes (0,0) and (0,1) except their shared edge (0,1)v.
        s = claimEdge(s, true, 0, 0)!!  // top of (0,0)
        s = claimEdge(s, true, 0, 1)!!  // top of (0,1)
        s = claimEdge(s, true, 1, 0)!!  // bottom of (0,0)
        s = claimEdge(s, true, 1, 1)!!  // bottom of (0,1)
        s = claimEdge(s, false, 0, 0)!! // left of (0,0)
        s = claimEdge(s, false, 0, 2)!! // right of (0,1)
        val turnBefore = s.turn
        s = claimEdge(s, false, 0, 1)!! // shared edge completes both boxes
        assertEquals(2, s.boxes.size)
        assertEquals(turnBefore, s.turn) // still the same player's turn
    }

    @Test fun gameFinishesWhenAllBoxesClaimed() {
        var s = DotsState(n = 1)
        s = claimEdge(s, true, 0, 0)!!
        s = claimEdge(s, true, 1, 0)!!
        s = claimEdge(s, false, 0, 0)!!
        s = claimEdge(s, false, 0, 1)!!
        assertTrue(s.finished)
        assertEquals(1, s.scoreOf1 + s.scoreOf2)
    }
}
