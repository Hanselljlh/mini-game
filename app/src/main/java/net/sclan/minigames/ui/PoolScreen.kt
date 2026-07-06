package net.sclan.minigames.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// Pure game logic — top-down pool on a 2:1 table (width 1.0, height 0.5).
// Balls are equal-mass circles; collisions are elastic. Six pockets capture
// balls that reach them. Practice rules: clear all object balls to win;
// pocketing the cue ball is a scratch (it respots).
// ---------------------------------------------------------------------------

internal const val POOL_W = 1.0f
internal const val POOL_H = 0.5f
internal const val POOL_R = 0.020f
internal const val POCKET_R = 0.040f
internal val POOL_CUE_SPOT = Offset(0.25f, 0.25f)

internal val POOL_POCKETS = listOf(
    Offset(0f, 0f), Offset(0.5f, 0f), Offset(1f, 0f),
    Offset(0f, 0.5f), Offset(0.5f, 0.5f), Offset(1f, 0.5f)
)

internal data class PoolBall(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val active: Boolean = true,
    val isCue: Boolean = false
)

internal fun rackPool(): List<PoolBall> {
    val balls = mutableListOf(PoolBall(0, POOL_CUE_SPOT.x, POOL_CUE_SPOT.y, isCue = true))
    val apexX = 0.60f
    val spacingX = POOL_R * 2 * 0.90f
    val spacingY = POOL_R * 2 * 1.02f
    var id = 1
    for (row in 0..2) {
        val count = row + 1
        val rowX = apexX + row * spacingX
        val topY = 0.25f - (count - 1) * spacingY / 2
        for (k in 0 until count) {
            balls.add(PoolBall(id, rowX, topY + k * spacingY))
            id++
        }
    }
    return balls
}

internal fun poolAllStopped(balls: List<PoolBall>): Boolean =
    balls.none { it.active && (it.vx != 0f || it.vy != 0f) }

internal fun poolObjectBallsLeft(balls: List<PoolBall>): Int =
    balls.count { it.active && !it.isCue }

private fun nearestPocket(x: Float, y: Float): Float =
    POOL_POCKETS.minOf { hypot(x - it.x, y - it.y) }

/** One physics substep: move + friction + wall/pocket + pairwise collisions. */
internal fun poolSubstep(balls: List<PoolBall>): List<PoolBall> {
    val moved = balls.map { b ->
        if (!b.active) return@map b
        var x = b.x + b.vx
        var y = b.y + b.vy
        var vx = b.vx * 0.985f
        var vy = b.vy * 0.985f
        // Pocket capture (check before wall clamp so corner balls fall in)
        if (nearestPocket(x, y) < POCKET_R) {
            return@map b.copy(active = false, vx = 0f, vy = 0f)
        }
        if (x < POOL_R) { x = POOL_R; vx = -vx }
        if (x > POOL_W - POOL_R) { x = POOL_W - POOL_R; vx = -vx }
        if (y < POOL_R) { y = POOL_R; vy = -vy }
        if (y > POOL_H - POOL_R) { y = POOL_H - POOL_R; vy = -vy }
        if (hypot(vx, vy) < 0.0006f) { vx = 0f; vy = 0f }
        b.copy(x = x, y = y, vx = vx, vy = vy)
    }.toMutableList()

    // Pairwise elastic collisions (equal mass)
    for (i in moved.indices) {
        for (j in i + 1 until moved.size) {
            val a = moved[i]
            val b = moved[j]
            if (!a.active || !b.active) continue
            val dx = b.x - a.x
            val dy = b.y - a.y
            val dist = hypot(dx, dy)
            val minDist = POOL_R * 2
            if (dist in 0.0001f..minDist) {
                val nx = dx / dist
                val ny = dy / dist
                // Separate the overlap
                val overlap = (minDist - dist) / 2
                val ax = a.x - nx * overlap
                val ay = a.y - ny * overlap
                val bx = b.x + nx * overlap
                val by = b.y + ny * overlap
                // Exchange velocity along the normal if approaching
                val dvx = a.vx - b.vx
                val dvy = a.vy - b.vy
                val p = dvx * nx + dvy * ny
                if (p > 0) {
                    moved[i] = a.copy(x = ax, y = ay, vx = a.vx - p * nx, vy = a.vy - p * ny)
                    moved[j] = b.copy(x = bx, y = by, vx = b.vx + p * nx, vy = b.vy + p * ny)
                } else {
                    moved[i] = a.copy(x = ax, y = ay)
                    moved[j] = b.copy(x = bx, y = by)
                }
            }
        }
    }
    return moved
}

/** Respots the cue ball at the head spot after a scratch. */
internal fun poolRespotCue(balls: List<PoolBall>): List<PoolBall> =
    balls.map { if (it.isCue) it.copy(x = POOL_CUE_SPOT.x, y = POOL_CUE_SPOT.y, vx = 0f, vy = 0f, active = true) else it }

internal fun poolCueScratched(balls: List<PoolBall>): Boolean =
    balls.any { it.isCue && !it.active }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val poolBallColors = listOf(
    Color(0xFFFFEB3B), Color(0xFF1E88E5), Color(0xFFE53935), Color(0xFF8E24AA),
    Color(0xFFFB8C00), Color(0xFF43A047), Color(0xFF6D4C41), Color(0xFF00ACC1)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoolScreen(
    onBack: () -> Unit,
    onWin: (shots: Int) -> Unit = {}
) {
    var balls by remember { mutableStateOf(rackPool()) }
    var shots by remember { mutableIntStateOf(0) }
    var animating by remember { mutableStateOf(false) }
    var aimStart by remember { mutableStateOf<Offset?>(null) }
    var aimCurrent by remember { mutableStateOf<Offset?>(null) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    var won by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }

    LaunchedEffect(animating) {
        while (animating) {
            delay(16)
            var next = balls
            repeat(3) { next = poolSubstep(next) }
            balls = next
            if (poolAllStopped(next)) {
                if (poolCueScratched(next)) balls = poolRespotCue(next)
                animating = false
                if (poolObjectBallsLeft(balls) == 0 && !reported) {
                    won = true
                    reported = true
                    onWin(shots)
                }
            }
        }
    }

    fun reset() {
        balls = rackPool()
        shots = 0
        animating = false
        won = false
        reported = false
        aimStart = null
        aimCurrent = null
    }

    val cue = balls.firstOrNull { it.isCue && it.active }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pool") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Shots: $shots  •  Balls left: ${poolObjectBallsLeft(balls)}", style = MaterialTheme.typography.titleMedium)
                if (won) Text("Cleared! 🎉", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("Rerack") }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(POOL_W / POOL_H)
                    .background(Color(0xFF1B5E20), RoundedCornerShape(14.dp))
                    .onSizeChanged { boardSize = it }
                    .pointerInput(animating, won) {
                        detectDragGestures(
                            onDragStart = { pos -> if (!animating && !won) aimStart = pos },
                            onDrag = { change, _ ->
                                change.consume()
                                if (!animating && !won) aimCurrent = change.position
                            },
                            onDragEnd = {
                                val c = cue
                                val start = aimStart
                                val end = aimCurrent
                                if (!animating && !won && c != null && start != null && end != null && boardSize.width > 0) {
                                    // Shoot from the drag direction: pull back, release like a slingshot.
                                    val dx = (start.x - end.x) / boardSize.width
                                    val dy = (start.y - end.y) / boardSize.height
                                    val power = hypot(dx, dy).coerceAtMost(0.5f)
                                    if (power > 0.01f) {
                                        val mag = sqrt(dx * dx + dy * dy)
                                        val speed = power * 0.09f
                                        balls = balls.map {
                                            if (it.isCue) it.copy(vx = dx / mag * speed, vy = dy / mag * speed) else it
                                        }
                                        shots++
                                        animating = true
                                    }
                                }
                                aimStart = null
                                aimCurrent = null
                            }
                        )
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    fun px(fx: Float, fy: Float) = Offset(fx / POOL_W * w, fy / POOL_H * h)
                    // Pockets
                    POOL_POCKETS.forEach {
                        drawCircle(Color(0xFF1A1A1A), radius = POCKET_R / POOL_W * w, center = px(it.x, it.y))
                    }
                    // Aim guide
                    val c = cue
                    if (c != null && aimStart != null && aimCurrent != null) {
                        val s = aimStart!!
                        val e = aimCurrent!!
                        val dirx = s.x - e.x
                        val diry = s.y - e.y
                        val len = hypot(dirx, diry).coerceAtLeast(1f)
                        val cueCenter = px(c.x, c.y)
                        drawLine(
                            Color.White.copy(alpha = 0.7f),
                            start = cueCenter,
                            end = Offset(cueCenter.x + dirx / len * 140f, cueCenter.y + diry / len * 140f),
                            strokeWidth = 3f
                        )
                    }
                    // Balls
                    balls.filter { it.active }.forEach { b ->
                        val color = if (b.isCue) Color.White else poolBallColors[(b.id - 1) % poolBallColors.size]
                        drawCircle(color, radius = POOL_R / POOL_W * w, center = px(b.x, b.y))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Drag back from the table and release to shoot the cue ball — like a slingshot. Longer drag = more power. Sink every colored ball. Scratching respots the cue.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
