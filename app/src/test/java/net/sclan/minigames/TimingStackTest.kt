package net.sclan.minigames

import net.sclan.minigames.ui.STACK_MAX_LAYERS
import net.sclan.minigames.ui.STACK_WIDTH
import net.sclan.minigames.ui.StackLayer
import net.sclan.minigames.ui.StackState
import net.sclan.minigames.ui.placeStack
import net.sclan.minigames.ui.tickStack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimingStackTest {

    @Test fun tickMovesRightThenBounces() {
        var s = StackState(moving = StackLayer(0, 4), movingRight = true)
        s = tickStack(s)
        assertEquals(1, s.moving.start)
        // Walk to the right edge
        repeat(STACK_WIDTH) { s = tickStack(s) }
        assertTrue(s.moving.start in 0..(STACK_WIDTH - s.moving.width))
    }

    @Test fun perfectPlacementKeepsWidth() {
        val base = StackLayer(4, 4)
        val s = StackState(layers = listOf(base), moving = StackLayer(4, 4))
        val placed = placeStack(s)
        assertEquals(2, placed.layers.size)
        assertEquals(4, placed.layers.last().width)
        assertFalse(placed.gameOver)
    }

    @Test fun partialOverlapTrims() {
        val base = StackLayer(4, 4) // covers 4..7
        val s = StackState(layers = listOf(base), moving = StackLayer(6, 4)) // covers 6..9
        val placed = placeStack(s)
        assertEquals(StackLayer(6, 2), placed.layers.last())
    }

    @Test fun noOverlapEndsGame() {
        val base = StackLayer(4, 4)
        val s = StackState(layers = listOf(base), moving = StackLayer(8, 4)) // 8..11 vs 4..7
        val placed = placeStack(s)
        assertTrue(placed.gameOver)
        assertEquals(1, placed.layers.size)
    }

    @Test fun reachingMaxLayersWins() {
        var s = StackState()
        repeat(STACK_MAX_LAYERS) {
            s = placeStack(s.copy(moving = StackLayer(s.layers.last().start, s.layers.last().width)))
        }
        assertTrue(s.won)
        assertEquals(STACK_MAX_LAYERS, s.score)
    }

    @Test fun scoreExcludesBase() {
        assertEquals(0, StackState().score)
    }
}
