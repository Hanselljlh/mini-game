package net.sclan.minigames.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — air hockey in normalized coords [0,1].
// Bottom player defends y≈1 (attacks the top goal); top defends y≈0.
// ---------------------------------------------------------------------------

internal const val AH_PUCK_R = 0.035f
internal const val AH_MALLET_R = 0.072f
internal const val AH_GOAL_HALF = 0.20f
internal const val AH_WIN = 7

internal data class AirHockeyState(
    val puckX: Float = 0.5f,
    val puckY: Float = 0.5f,
    val velX: Float = 0f,
    val velY: Float = 0f,
    val bottomScore: Int = 0,
    val topScore: Int = 0
) {
    val winner: Int? get() = when {
        bottomScore >= AH_WIN -> 1
        topScore >= AH_WIN -> 2
        else -> null
    }
}

internal fun ahServe(towardTop: Boolean, random: Random = Random.Default): AirHockeyState =
    AirHockeyState(
        puckX = 0.5f,
        puckY = 0.5f,
        velX = (random.nextFloat() - 0.5f) * 0.01f,
        velY = (if (towardTop) -1f else 1f) * 0.012f
    )

/** Reflects the puck off a mallet modeled as a static circle, with a speed boost. */
private fun bounceOffMallet(s: AirHockeyState, mx: Float, my: Float): AirHockeyState {
    val dx = s.puckX - mx
    val dy = s.puckY - my
    val dist = hypot(dx, dy)
    val minDist = AH_PUCK_R + AH_MALLET_R
    if (dist >= minDist || dist == 0f) return s
    val nx = dx / dist
    val ny = dy / dist
    // Push out and reflect velocity along the normal, then add a kick outward.
    val speed = hypot(s.velX, s.velY).coerceAtLeast(0.010f)
    return s.copy(
        puckX = mx + nx * minDist,
        puckY = my + ny * minDist,
        velX = nx * (speed + 0.006f),
        velY = ny * (speed + 0.006f)
    )
}

internal fun ahTick(
    s: AirHockeyState,
    bottomMallet: Offset,
    topMallet: Offset,
    speed: Float,
    random: Random = Random.Default
): AirHockeyState {
    var st = s.copy(puckX = s.puckX + s.velX * speed, puckY = s.puckY + s.velY * speed)
    // Side walls
    if (st.puckX - AH_PUCK_R < 0f) st = st.copy(puckX = AH_PUCK_R, velX = -st.velX)
    if (st.puckX + AH_PUCK_R > 1f) st = st.copy(puckX = 1f - AH_PUCK_R, velX = -st.velX)
    // Top edge / goal
    if (st.puckY - AH_PUCK_R < 0f) {
        if (kotlin.math.abs(st.puckX - 0.5f) < AH_GOAL_HALF) {
            return ahServe(towardTop = true, random = random).copy(bottomScore = st.bottomScore + 1, topScore = st.topScore)
        }
        st = st.copy(puckY = AH_PUCK_R, velY = -st.velY)
    }
    // Bottom edge / goal
    if (st.puckY + AH_PUCK_R > 1f) {
        if (kotlin.math.abs(st.puckX - 0.5f) < AH_GOAL_HALF) {
            return ahServe(towardTop = false, random = random).copy(topScore = st.topScore + 1, bottomScore = st.bottomScore)
        }
        st = st.copy(puckY = 1f - AH_PUCK_R, velY = -st.velY)
    }
    // Mallets
    st = bounceOffMallet(st, bottomMallet.x, bottomMallet.y)
    st = bounceOffMallet(st, topMallet.x, topMallet.y)
    // Mild friction, clamp speed
    val fr = 0.999f
    val vx = (st.velX * fr).coerceIn(-0.03f, 0.03f)
    val vy = (st.velY * fr).coerceIn(-0.03f, 0.03f)
    return st.copy(velX = vx, velY = vy)
}

/** Bot mallet chases the puck within the top half; guards its goal when the puck is far. */
internal fun ahBotMallet(current: Offset, s: AirHockeyState, maxStep: Float): Offset {
    val targetX: Float
    val targetY: Float
    if (s.puckY < 0.5f) {
        targetX = s.puckX
        targetY = (s.puckY - 0.05f).coerceIn(AH_MALLET_R, 0.45f)
    } else {
        targetX = 0.5f
        targetY = 0.12f
    }
    val dx = (targetX - current.x).coerceIn(-maxStep, maxStep)
    val dy = (targetY - current.y).coerceIn(-maxStep, maxStep)
    return Offset(
        (current.x + dx).coerceIn(AH_MALLET_R, 1f - AH_MALLET_R),
        (current.y + dy).coerceIn(AH_MALLET_R, 0.5f - AH_MALLET_R)
    )
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirHockeyScreen(
    mode: PongMode = PongMode.VsBot,
    onBack: () -> Unit,
    onFinished: (playerWon: Boolean) -> Unit = {}
) {
    var state by remember(mode) { mutableStateOf(ahServe(towardTop = true)) }
    var bottomMallet by remember(mode) { mutableStateOf(Offset(0.5f, 0.88f)) }
    var topMallet by remember(mode) { mutableStateOf(Offset(0.5f, 0.12f)) }
    var running by remember(mode) { mutableStateOf(false) }
    var reported by remember(mode) { mutableStateOf(false) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(running, mode) {
        while (running) {
            delay(16)
            if (mode == PongMode.VsBot || mode == PongMode.FastBot) {
                topMallet = ahBotMallet(topMallet, state, mode.botStep + 0.004f)
            }
            val next = ahTick(state, bottomMallet, topMallet, mode.speed)
            state = next
            if (next.winner != null) {
                running = false
                if (!reported) {
                    reported = true
                    onFinished(next.winner == 1)
                }
            }
        }
    }

    fun reset() {
        state = ahServe(towardTop = true)
        bottomMallet = Offset(0.5f, 0.88f)
        topMallet = Offset(0.5f, 0.12f)
        running = false
        reported = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Air Hockey • ${mode.label}") },
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
                Text(
                    "${if (mode == PongMode.TwoPlayer) "Top" else "Bot"} ${state.topScore}  —  ${state.bottomScore} You",
                    fontWeight = FontWeight.Bold
                )
                when {
                    state.winner == 1 -> Text("You win! 🏆", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    state.winner == 2 -> Text("Opponent wins!", color = Color.Red, fontWeight = FontWeight.Bold)
                    !running -> Text("Drag to start", style = MaterialTheme.typography.bodySmall)
                    else -> {}
                }
                Button(onClick = ::reset) { Text("New Match") }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(16.dp))
                    .onSizeChanged { boardSize = it }
                    .pointerInput(mode) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            if (state.winner != null || boardSize.width == 0) return@detectDragGestures
                            running = true
                            val fx = (change.position.x / boardSize.width).coerceIn(AH_MALLET_R, 1f - AH_MALLET_R)
                            val fy = (change.position.y / boardSize.height)
                            val topHalf = fy < 0.5f
                            if (mode == PongMode.TwoPlayer && topHalf) {
                                topMallet = Offset(fx, fy.coerceIn(AH_MALLET_R, 0.5f - AH_MALLET_R))
                            } else {
                                bottomMallet = Offset(fx, fy.coerceIn(0.5f + AH_MALLET_R, 1f - AH_MALLET_R))
                            }
                        }
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    // Center line + circle
                    drawRect(Color(0xFF90CAF9), topLeft = Offset(0f, h / 2 - 2), size = Size(w, 4f))
                    drawCircle(Color(0xFF90CAF9), radius = w * 0.12f, center = Offset(w / 2, h / 2), style = androidx.compose.ui.graphics.drawscope.Stroke(4f))
                    // Goals
                    drawRect(Color(0xFFF06292), topLeft = Offset((0.5f - AH_GOAL_HALF) * w, 0f), size = Size(AH_GOAL_HALF * 2 * w, 6f))
                    drawRect(Color(0xFF4FC3F7), topLeft = Offset((0.5f - AH_GOAL_HALF) * w, h - 6f), size = Size(AH_GOAL_HALF * 2 * w, 6f))
                    // Mallets
                    drawCircle(Color(0xFFF06292), radius = AH_MALLET_R * w, center = Offset(topMallet.x * w, topMallet.y * h))
                    drawCircle(Color(0xFF1E88E5), radius = AH_MALLET_R * w, center = Offset(bottomMallet.x * w, bottomMallet.y * h))
                    // Puck
                    drawCircle(Color(0xFF212121), radius = AH_PUCK_R * w, center = Offset(state.puckX * w, state.puckY * h))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (mode == PongMode.TwoPlayer)
                    "Two players: each drags their mallet on their own half. Knock the puck into the far goal. First to $AH_WIN."
                else
                    "Drag your blue mallet to strike the puck into the top goal. First to $AH_WIN wins.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
