package net.sclan.minigames

import net.sclan.minigames.ui.POCKET_R
import net.sclan.minigames.ui.POOL_POCKETS
import net.sclan.minigames.ui.POOL_R
import net.sclan.minigames.ui.PoolBall
import net.sclan.minigames.ui.poolAllStopped
import net.sclan.minigames.ui.poolCueScratched
import net.sclan.minigames.ui.poolObjectBallsLeft
import net.sclan.minigames.ui.poolRespotCue
import net.sclan.minigames.ui.poolSubstep
import net.sclan.minigames.ui.rackPool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class PoolTest {

    @Test fun rackHasCuePlusSixObjectBalls() {
        val balls = rackPool()
        assertEquals(1, balls.count { it.isCue })
        assertEquals(6, poolObjectBallsLeft(balls))
        // No two balls start overlapping
        for (i in balls.indices) for (j in i + 1 until balls.size) {
            val d = hypot(balls[i].x - balls[j].x, balls[i].y - balls[j].y)
            assertTrue("balls $i,$j overlap ($d)", d >= POOL_R * 2 - 0.0001f)
        }
    }

    @Test fun frictionBringsBallToRest() {
        var balls = listOf(PoolBall(1, 0.3f, 0.25f, vx = 0.02f, vy = 0f))
        repeat(2000) { balls = poolSubstep(balls) }
        assertTrue(poolAllStopped(balls))
    }

    @Test fun ballBouncesOffCushion() {
        // Moving up toward the top rail, away from any pocket (x = 0.25)
        var balls = listOf(PoolBall(1, 0.25f, POOL_R + 0.002f, vx = 0f, vy = -0.02f))
        balls = poolSubstep(balls)
        assertTrue(balls[0].vy > 0)
        assertTrue(balls[0].active)
    }

    @Test fun ballFallsIntoPocket() {
        val corner = POOL_POCKETS[0] // (0,0)
        var balls = listOf(PoolBall(1, corner.x + POCKET_R * 0.5f, corner.y + POCKET_R * 0.5f, vx = -0.01f, vy = -0.01f))
        balls = poolSubstep(balls)
        assertFalse(balls[0].active)
    }

    @Test fun headOnCollisionTransfersMomentum() {
        // Ball A moving right into stationary ball B just to its right.
        val a = PoolBall(1, 0.30f, 0.25f, vx = 0.02f, vy = 0f)
        val b = PoolBall(2, 0.30f + POOL_R * 2 * 0.99f, 0.25f, vx = 0f, vy = 0f)
        val next = poolSubstep(listOf(a, b))
        val na = next.first { it.id == 1 }
        val nb = next.first { it.id == 2 }
        // A should slow, B should pick up rightward speed
        assertTrue(nb.vx > 0.01f)
        assertTrue(na.vx < a.vx)
    }

    @Test fun collisionSeparatesOverlap() {
        val a = PoolBall(1, 0.30f, 0.25f, vx = 0f, vy = 0f)
        val b = PoolBall(2, 0.30f + POOL_R, 0.25f, vx = 0f, vy = 0f) // heavily overlapping
        val next = poolSubstep(listOf(a, b))
        val d = hypot(next[0].x - next[1].x, next[0].y - next[1].y)
        assertTrue(d > POOL_R) // pushed apart
    }

    @Test fun respotReactivatesCue() {
        val scratched = rackPool().map { if (it.isCue) it.copy(active = false) else it }
        assertTrue(poolCueScratched(scratched))
        val respotted = poolRespotCue(scratched)
        assertFalse(poolCueScratched(respotted))
        val cue = respotted.first { it.isCue }
        assertTrue(cue.active)
        assertEquals(0.25f, cue.x, 0.001f)
    }
}
