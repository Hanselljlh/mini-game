package net.sclan.minigames

import net.sclan.minigames.ui.MancalaState
import net.sclan.minigames.ui.mancalaBotMove
import net.sclan.minigames.ui.mancalaValidMoves
import net.sclan.minigames.ui.sowMancala
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MancalaTest {

    @Test fun initialBoardHas48Seeds() {
        val s = MancalaState()
        assertEquals(48, s.pits.sum())
        assertEquals(0, s.store1)
        assertEquals(0, s.store2)
        assertEquals((0..5).toList(), mancalaValidMoves(s))
    }

    @Test fun seedCountIsConservedBySowing() {
        var s: MancalaState? = MancalaState()
        s = sowMancala(s!!, 2)
        assertEquals(48, s!!.pits.sum())
    }

    @Test fun sowingFromPit2LandsInStoreForExtraTurn() {
        // Pit 2 has 4 seeds → lands in pits 3,4,5 and store (index 6) → extra turn
        val s = sowMancala(MancalaState(), 2)!!
        assertEquals(1, s.store1)
        assertEquals(1, s.turn) // extra turn
        assertEquals(0, s.pits[2])
        assertEquals(5, s.pits[3])
    }

    @Test fun sowingWithoutStoreLandingPassesTurn() {
        val s = sowMancala(MancalaState(), 0)!! // lands in pit 4
        assertEquals(2, s.turn)
    }

    @Test fun opponentStoreIsSkipped() {
        // Pit 5 with 9 seeds: sows 6(store),7..12, skips 13, then 0,1. The last
        // seed lands in empty own pit 1 and captures the seed opposite (pit 11):
        // store = 1 (sown) + 1 (last seed) + 1 (captured) = 3.
        val pits = MutableList(14) { 0 }
        pits[5] = 9
        pits[0] = 1 // keep side 1 non-empty after move
        val s = sowMancala(MancalaState(pits = pits, turn = 1), 5)!!
        assertEquals(0, s.pits[13]) // opponent store untouched
        assertEquals(3, s.pits[6])
        assertEquals(0, s.pits[11]) // captured
    }

    @Test fun captureTakesOppositeSeeds() {
        // Player 1 sows 1 seed from pit 0 into empty pit 1; pit 11 (opposite of 1) has seeds.
        val pits = MutableList(14) { 0 }
        pits[0] = 1
        pits[11] = 5
        pits[7] = 1 // keep side 2 non-empty
        val s = sowMancala(MancalaState(pits = pits, turn = 1), 0)!!
        // Captured: 1 (own last seed) + 5 (opposite) = 6 in store... but side 1 became empty → sweep
        assertEquals(6, s.store1)
        assertEquals(0, s.pits[11])
        assertEquals(0, s.pits[1])
    }

    @Test fun emptyPitIsIllegalMove() {
        val pits = MutableList(14) { 4 }
        pits[3] = 0
        assertNull(sowMancala(MancalaState(pits = pits, turn = 1), 3))
        assertNull(sowMancala(MancalaState(), 7)) // opponent's pit on your turn
    }

    @Test fun gameEndsWithSweep() {
        val pits = MutableList(14) { 0 }
        pits[5] = 1  // player 1's only seed
        pits[7] = 3
        pits[10] = 2
        val s = sowMancala(MancalaState(pits = pits, turn = 1), 5)!!
        assertTrue(s.finished)
        assertEquals(1, s.store1)          // the sown seed reached the store
        assertEquals(5, s.store2)          // side 2 swept its 5 remaining seeds
        assertTrue((0..5).all { s.pits[it] == 0 })
        assertTrue((7..12).all { s.pits[it] == 0 })
    }

    @Test fun botPrefersExtraTurnMove() {
        // Pit 9 with 4 seeds lands exactly in store 13 → extra turn; bot should pick it
        val pits = MutableList(14) { 0 }
        pits[9] = 4
        pits[8] = 6
        pits[0] = 1
        val s = MancalaState(pits = pits, turn = 2)
        assertEquals(9, mancalaBotMove(s))
    }

    @Test fun botReturnsNullWhenNoMoves() {
        val pits = MutableList(14) { 0 }
        pits[0] = 2
        assertNull(mancalaBotMove(MancalaState(pits = pits, turn = 2)))
    }
}
