package net.sclan.minigames

import net.sclan.minigames.ui.extendSimonSequence
import net.sclan.minigames.ui.simonScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SimonSaysTest {

    @Test fun extendAddsExactlyOnePad() {
        val seq = extendSimonSequence(listOf(0, 1, 2), Random(1))
        assertEquals(4, seq.size)
        assertEquals(listOf(0, 1, 2), seq.take(3))
    }

    @Test fun extendedPadIsInRange() {
        repeat(50) { seed ->
            val seq = extendSimonSequence(emptyList(), Random(seed))
            assertTrue(seq.single() in 0..3)
        }
    }

    @Test fun scoreCountsCompletedRoundsOnFailure() {
        // Failing on a sequence of length 5 means 4 rounds were fully repeated
        assertEquals(4, simonScore(5, failed = true))
    }

    @Test fun scoreNeverNegative() {
        assertEquals(0, simonScore(0, failed = true))
        assertEquals(0, simonScore(1, failed = true))
    }

    @Test fun scoreWithoutFailureIsFullLength() {
        assertEquals(7, simonScore(7, failed = false))
    }
}
