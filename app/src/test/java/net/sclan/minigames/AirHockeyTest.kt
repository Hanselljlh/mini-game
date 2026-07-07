package net.sclan.minigames

import androidx.compose.ui.geometry.Offset
import net.sclan.minigames.ui.AH_GOAL_HALF
import net.sclan.minigames.ui.AH_MALLET_R
import net.sclan.minigames.ui.AH_PUCK_R
import net.sclan.minigames.ui.AH_WIN
import net.sclan.minigames.ui.AirHockeyState
import net.sclan.minigames.ui.ahBotMallet
import net.sclan.minigames.ui.ahServe
import net.sclan.minigames.ui.ahTick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot
import kotlin.random.Random

class AirHockeyTest {

    private val farMallet = Offset(-1f, -1f) // out of play, never collides

    @Test fun serveStartsCentered() {
        val s = ahServe(towardTop = true, random = Random(1))
        assertEquals(0.5f, s.puckX, 0.001f)
        assertEquals(0.5f, s.puckY, 0.001f)
        assertTrue(s.velY < 0) // heading toward top
    }

    @Test fun puckBouncesOffSideWall() {
        val s = AirHockeyState(puckX = AH_PUCK_R + 0.002f, puckY = 0.5f, velX = -0.02f, velY = 0f)
        val next = ahTick(s, farMallet, farMallet, 1f, Random(1))
        assertTrue(next.velX > 0)
    }

    @Test fun puckBouncesOffFlatEndOutsideGoal() {
        // Heading into the top wall but off to the side (outside goal mouth)
        val s = AirHockeyState(puckX = 0.9f, puckY = AH_PUCK_R + 0.002f, velX = 0f, velY = -0.02f)
        val next = ahTick(s, farMallet, farMallet, 1f, Random(1))
        assertTrue(next.velY > 0)
        assertEquals(0, next.bottomScore)
    }

    @Test fun goalThroughTopScoresBottomPlayer() {
        val s = AirHockeyState(puckX = 0.5f, puckY = AH_PUCK_R - 0.005f, velX = 0f, velY = -0.02f)
        assertTrue(kotlin.math.abs(s.puckX - 0.5f) < AH_GOAL_HALF)
        val next = ahTick(s, farMallet, farMallet, 1f, Random(1))
        assertEquals(1, next.bottomScore)
        assertEquals(0.5f, next.puckY, 0.001f) // re-served
    }

    @Test fun goalThroughBottomScoresTopPlayer() {
        val s = AirHockeyState(puckX = 0.5f, puckY = 1f - AH_PUCK_R + 0.005f, velX = 0f, velY = 0.02f)
        val next = ahTick(s, farMallet, farMallet, 1f, Random(1))
        assertEquals(1, next.topScore)
    }

    @Test fun malletKnocksPuckAway() {
        // Puck overlapping the bottom mallet; should be pushed out and moving away.
        val mallet = Offset(0.5f, 0.8f)
        val s = AirHockeyState(puckX = 0.5f, puckY = 0.8f - AH_PUCK_R, velX = 0f, velY = 0.001f)
        val next = ahTick(s, mallet, farMallet, 1f, Random(1))
        val dist = hypot(next.puckX - mallet.x, next.puckY - mallet.y)
        assertTrue(dist >= AH_PUCK_R + AH_MALLET_R - 0.001f)
        assertTrue(next.velY < 0) // knocked upward, away from the bottom mallet
    }

    @Test fun botStaysInTopHalf() {
        var mallet = Offset(0.5f, 0.12f)
        val s = AirHockeyState(puckX = 0.3f, puckY = 0.3f)
        repeat(30) { mallet = ahBotMallet(mallet, s, 0.01f) }
        assertTrue(mallet.y <= 0.5f)
        assertTrue(mallet.x in AH_MALLET_R..(1f - AH_MALLET_R))
    }

    @Test fun winnerDetected() {
        assertEquals(1, AirHockeyState(bottomScore = AH_WIN).winner)
        assertEquals(2, AirHockeyState(topScore = AH_WIN).winner)
        assertNull(AirHockeyState(bottomScore = AH_WIN - 1, topScore = AH_WIN - 1).winner)
    }
}
