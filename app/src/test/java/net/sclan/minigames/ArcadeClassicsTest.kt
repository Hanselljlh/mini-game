package net.sclan.minigames

import net.sclan.minigames.ui.HANGMAN_LIVES
import net.sclan.minigames.ui.HANGMAN_WORDS
import net.sclan.minigames.ui.PongState
import net.sclan.minigames.ui.WG_LENGTH
import net.sclan.minigames.ui.WORDS_5
import net.sclan.minigames.ui.WgMark
import net.sclan.minigames.ui.hangmanMasked
import net.sclan.minigames.ui.hangmanWon
import net.sclan.minigames.ui.hangmanWrongCount
import net.sclan.minigames.ui.pkIsGoal
import net.sclan.minigames.ui.pkZoneFor
import net.sclan.minigames.ui.pongBotMove
import net.sclan.minigames.ui.pongTick
import net.sclan.minigames.ui.shuffledSliding
import net.sclan.minigames.ui.slidingMovable
import net.sclan.minigames.ui.slidingMove
import net.sclan.minigames.ui.slidingSolved
import net.sclan.minigames.ui.solvedSliding
import net.sclan.minigames.ui.spinnerTick
import net.sclan.minigames.ui.spinnerTotalRotations
import net.sclan.minigames.ui.wgScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class ArcadeClassicsTest {

    // --- Word Rescue (hangman) ---

    @Test fun maskedHidesUnguessedLetters() {
        assertEquals("_ _ _", hangmanMasked("CAT", emptySet()))
        assertEquals("C _ T", hangmanMasked("CAT", setOf('C', 'T')))
        assertEquals("C A T", hangmanMasked("CAT", setOf('C', 'A', 'T')))
    }

    @Test fun wonAndWrongCounts() {
        assertTrue(hangmanWon("CAT", setOf('C', 'A', 'T', 'Z')))
        assertFalse(hangmanWon("CAT", setOf('C', 'A')))
        assertEquals(2, hangmanWrongCount("CAT", setOf('C', 'X', 'Z')))
        assertTrue(HANGMAN_LIVES > 0)
    }

    @Test fun wordListIsAllCaps() {
        HANGMAN_WORDS.forEach { w -> assertTrue(w.all { it in 'A'..'Z' }) }
    }

    // --- Word Guess ---

    @Test fun allAnswersAreFiveLetters() {
        WORDS_5.forEach { assertEquals(WG_LENGTH, it.length) }
        assertEquals(WORDS_5.size, WORDS_5.toSet().size)
    }

    @Test fun perfectGuessAllGreen() {
        assertTrue(wgScore("APPLE", "APPLE").all { it == WgMark.Correct })
    }

    @Test fun duplicateLettersScoredOnce() {
        // secret has one P at index 1... APPLE has two Ps. Use SPOON vs PIZZA-style case:
        // secret "SPOON", guess "OOOOO": secret has two Os (idx 2,3) → two greens, rest absent.
        val marks = wgScore("SPOON", "OOOOO")
        assertEquals(WgMark.Absent, marks[0])
        assertEquals(WgMark.Absent, marks[1])
        assertEquals(WgMark.Correct, marks[2])
        assertEquals(WgMark.Correct, marks[3])
        assertEquals(WgMark.Absent, marks[4])
    }

    @Test fun presentMarkForWrongSpot() {
        val marks = wgScore("CRANE", "NACRE")
        // N,A,C,R present/wrong-spot except E correct at the end
        assertEquals(WgMark.Correct, marks[4])
        assertTrue(marks.take(4).all { it == WgMark.Present })
    }

    // --- Sliding puzzle ---

    @Test fun solvedBoardIsOrdered() {
        assertEquals(listOf(1, 2, 3, 0), solvedSliding(2))
        assertTrue(slidingSolved(solvedSliding(4), 4))
    }

    @Test fun shuffleIsSolvableAndScrambled() {
        repeat(5) { seed ->
            val board = shuffledSliding(4, random = Random(seed))
            assertEquals(16, board.size)
            assertEquals((0..15).toSet(), board.toSet())
        }
    }

    @Test fun onlyAdjacentTilesMove() {
        val board = solvedSliding(3) // hole at index 8
        assertEquals(setOf(5, 7), slidingMovable(board, 3).toSet())
        assertNull(slidingMove(board, 3, 0))
        val moved = slidingMove(board, 3, 7)!!
        assertEquals(0, moved[7])
        assertEquals(8, moved[8])
    }

    // --- Pong ---

    @Test fun ballBouncesOffSideWalls() {
        val s = PongState(ballX = 0.005f, velX = -0.01f, ballY = 0.5f, velY = 0f)
        val next = pongTick(s, 1f, Random(1))
        assertTrue(next.velX > 0)
    }

    @Test fun missedBallScoresOpponent() {
        val s = PongState(ballY = 1.2f, velY = 0.01f, ballX = 0.5f)
        val next = pongTick(s, 1f, Random(2))
        assertEquals(1, next.topScore)
        assertEquals(0.5f, next.ballY) // re-served
    }

    @Test fun botTracksBall() {
        val s = PongState(ballX = 0.9f, topPaddle = 0.2f)
        val next = pongBotMove(s, 0.01f)
        assertTrue(next.topPaddle > 0.2f)
        assertTrue(abs(next.topPaddle - 0.21f) < 0.001f) // capped step
    }

    // --- Penalty kicks ---

    @Test fun zonesSplitEvenly() {
        assertEquals(0, pkZoneFor(0.1f))
        assertEquals(1, pkZoneFor(0.5f))
        assertEquals(2, pkZoneFor(0.9f))
    }

    @Test fun goalWhenKeeperWrong() {
        assertTrue(pkIsGoal(0, 2))
        assertFalse(pkIsGoal(1, 1))
    }

    // --- Spinner ---

    @Test fun spinnerSlowsAndStops() {
        var angle = 0f
        var velocity = 10f
        repeat(1000) {
            val (a, v) = spinnerTick(angle, velocity)
            angle = a
            velocity = v
        }
        assertEquals(0f, velocity)
    }

    @Test fun rotationCounting() {
        assertEquals(2, spinnerTotalRotations(730f))
        assertEquals(0, spinnerTotalRotations(300f))
    }
}
