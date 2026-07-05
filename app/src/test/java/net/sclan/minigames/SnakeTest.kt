package net.sclan.minigames

import net.sclan.minigames.ui.SNAKE_COLS
import net.sclan.minigames.ui.SNAKE_ROWS
import net.sclan.minigames.ui.SnakeDir
import net.sclan.minigames.ui.SnakeState
import net.sclan.minigames.ui.newSnakeState
import net.sclan.minigames.ui.spawnFood
import net.sclan.minigames.ui.stepSnake
import net.sclan.minigames.ui.turnSnake
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SnakeTest {

    @Test fun newGameHasThreeSegmentsHeadingRight() {
        val s = newSnakeState(Random(1))
        assertEquals(3, s.body.size)
        assertEquals(SnakeDir.Right, s.dir)
        assertTrue(s.alive)
        assertEquals(0, s.score)
    }

    @Test fun foodNeverSpawnsOnSnake() {
        repeat(50) { seed ->
            val s = newSnakeState(Random(seed))
            assertFalse(s.food in s.body)
        }
    }

    @Test fun stepMovesHeadForward() {
        val s = newSnakeState(Random(1))
        val head = s.body.first()
        val next = stepSnake(s, Random(2))
        if (next.alive && next.score == 0) {
            assertEquals((head.first + 1) to head.second, next.body.first())
            assertEquals(3, next.body.size)
        }
    }

    @Test fun eatingFoodGrowsAndScores() {
        val base = newSnakeState(Random(1))
        // Place food directly in the snake's path
        val head = base.body.first()
        val fed = base.copy(food = (head.first + 1) to head.second)
        val next = stepSnake(fed, Random(3))
        assertEquals(1, next.score)
        assertEquals(4, next.body.size)
        assertFalse(next.food in next.body)
    }

    @Test fun hittingWallDies() {
        val atEdge = SnakeState(
            body = listOf((SNAKE_COLS - 1) to 5, (SNAKE_COLS - 2) to 5),
            dir = SnakeDir.Right,
            food = 0 to 0
        )
        val next = stepSnake(atEdge)
        assertFalse(next.alive)
    }

    @Test fun hittingSelfDies() {
        // A snake coiled so the head will run into its body
        val coiled = SnakeState(
            body = listOf(5 to 5, 5 to 6, 6 to 6, 6 to 5, 6 to 4, 5 to 4, 4 to 4),
            dir = SnakeDir.Left, // head at (5,5) moving left → (4,5)? no; Left = dx -1 → (4,5) empty
            food = 0 to 0
        )
        // Move head into (6,5) which is occupied: use Down (dy +1) → (5,6) occupied
        val next = stepSnake(coiled.copy(dir = SnakeDir.Down))
        assertFalse(next.alive)
    }

    @Test fun movingIntoTailTipIsSafe() {
        // 2x2 loop: head can chase its own tail tip safely
        val loop = SnakeState(
            body = listOf(5 to 5, 6 to 5, 6 to 6, 5 to 6),
            dir = SnakeDir.Down, // head (5,5) → (5,6), currently the tail tip
            food = 0 to 0
        )
        val next = stepSnake(loop)
        assertTrue(next.alive)
    }

    @Test fun cannotReverseIntoNeck() {
        val s = newSnakeState(Random(1)) // heading Right
        val turned = turnSnake(s, SnakeDir.Left)
        assertEquals(SnakeDir.Right, turned.dir)
        val ok = turnSnake(s, SnakeDir.Up)
        assertEquals(SnakeDir.Up, ok.dir)
    }

    @Test fun deadSnakeDoesNotMove() {
        val dead = newSnakeState(Random(1)).copy(alive = false)
        assertEquals(dead, stepSnake(dead))
    }

    @Test fun spawnFoodStaysOnBoard() {
        val s = newSnakeState(Random(9))
        repeat(30) {
            val f = spawnFood(s.body, Random(it))
            assertTrue(f.first in 0 until SNAKE_COLS)
            assertTrue(f.second in 0 until SNAKE_ROWS)
        }
    }
}
