package net.sclan.minigames

import net.sclan.minigames.ui.CODE_LENGTH
import net.sclan.minigames.ui.codeFeedback
import net.sclan.minigames.ui.newSecretCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CodeBreakerTest {

    @Test fun secretHasCorrectLengthAndRange() {
        repeat(20) { seed ->
            val secret = newSecretCode(6, Random(seed))
            assertEquals(CODE_LENGTH, secret.size)
            assertTrue(secret.all { it in 0 until 6 })
        }
    }

    @Test fun perfectGuessIsAllExact() {
        val secret = listOf(1, 2, 3, 4)
        assertEquals(4 to 0, codeFeedback(secret, secret))
    }

    @Test fun completeMissIsZeroZero() {
        assertEquals(0 to 0, codeFeedback(listOf(0, 0, 1, 1), listOf(2, 2, 3, 3)))
    }

    @Test fun rightColorsWrongSpots() {
        assertEquals(0 to 4, codeFeedback(listOf(1, 2, 3, 4), listOf(4, 3, 2, 1)))
    }

    @Test fun duplicatesInGuessDontOvercount() {
        // secret has one 1; guess has four 1s → only one match total, at position 0 (exact)
        assertEquals(1 to 0, codeFeedback(listOf(1, 2, 3, 4), listOf(1, 1, 1, 1)))
    }

    @Test fun duplicatesInSecretCountedCorrectly() {
        // secret 1,1,2,2 vs guess 1,2,1,2 → exact at 0 and 3; partials at 1,2
        assertEquals(2 to 2, codeFeedback(listOf(1, 1, 2, 2), listOf(1, 2, 1, 2)))
    }

    @Test fun mixedExactAndPartial() {
        assertEquals(1 to 2, codeFeedback(listOf(0, 1, 2, 3), listOf(0, 2, 1, 5)))
    }
}
