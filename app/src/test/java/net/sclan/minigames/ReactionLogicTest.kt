package net.sclan.minigames

import net.sclan.minigames.ui.averageMs
import net.sclan.minigames.ui.reactionRating
import org.junit.Assert.assertEquals
import org.junit.Test

class ReactionLogicTest {

    @Test fun averageOfEmptyIsZero() = assertEquals(0L, averageMs(emptyList()))

    @Test fun averageOfSingle() = assertEquals(250L, averageMs(listOf(250L)))

    @Test fun averageOfSeveral() = assertEquals(300L, averageMs(listOf(200L, 300L, 400L)))

    @Test fun ratingBoundaries() {
        assertEquals("—", reactionRating(0L))
        assertEquals("Lightning ⚡", reactionRating(249L))
        assertEquals("Quick", reactionRating(250L))
        assertEquals("Quick", reactionRating(349L))
        assertEquals("Steady", reactionRating(350L))
        assertEquals("Steady", reactionRating(499L))
        assertEquals("Warming Up", reactionRating(500L))
    }
}
