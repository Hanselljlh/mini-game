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
import kotlin.math.abs
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — pong duel in normalized coordinates (x, y in [0,1]).
// Player paddle at the bottom, opponent (bot or player 2) at the top.
// ---------------------------------------------------------------------------

internal const val PONG_PADDLE_W = 0.22f
internal const val PONG_PADDLE_H = 0.02f
internal const val PONG_BALL_R = 0.018f
internal const val PONG_WIN_SCORE = 5

internal data class PongState(
    val ballX: Float = 0.5f,
    val ballY: Float = 0.5f,
    val velX: Float = 0.007f,
    val velY: Float = 0.012f,
    val bottomPaddle: Float = 0.5f, // center x
    val topPaddle: Float = 0.5f,
    val bottomScore: Int = 0,
    val topScore: Int = 0
) {
    val winner: Int? get() = when {
        bottomScore >= PONG_WIN_SCORE -> 1
        topScore >= PONG_WIN_SCORE -> 2
        else -> null
    }
}

internal fun pongServe(state: PongState, random: Random = Random.Default): PongState =
    state.copy(
        ballX = 0.5f,
        ballY = 0.5f,
        velX = (if (random.nextBoolean()) 1 else -1) * (0.005f + random.nextFloat() * 0.004f),
        velY = (if (random.nextBoolean()) 1 else -1) * 0.011f
    )

/** One physics tick. Returns the new state; scores update when the ball passes a paddle. */
internal fun pongTick(state: PongState, speedMul: Float, random: Random = Random.Default): PongState {
    var x = state.ballX + state.velX * speedMul
    var y = state.ballY + state.velY * speedMul
    var vx = state.velX
    var vy = state.velY
    // Side walls
    if (x - PONG_BALL_R < 0f) { x = PONG_BALL_R; vx = -vx }
    if (x + PONG_BALL_R > 1f) { x = 1f - PONG_BALL_R; vx = -vx }
    // Bottom paddle (y ≈ 0.96)
    if (vy > 0 && y + PONG_BALL_R >= 0.96f && y + PONG_BALL_R <= 0.99f &&
        abs(x - state.bottomPaddle) <= PONG_PADDLE_W / 2
    ) {
        vy = -abs(vy) * 1.03f
        vx += (x - state.bottomPaddle) * 0.05f
        y = 0.96f - PONG_BALL_R
    }
    // Top paddle (y ≈ 0.04)
    if (vy < 0 && y - PONG_BALL_R <= 0.04f && y - PONG_BALL_R >= 0.01f &&
        abs(x - state.topPaddle) <= PONG_PADDLE_W / 2
    ) {
        vy = abs(vy) * 1.03f
        vx += (x - state.topPaddle) * 0.05f
        y = 0.04f + PONG_BALL_R
    }
    // Goals
    if (y > 1.05f) return pongServe(state.copy(topScore = state.topScore + 1), random)
    if (y < -0.05f) return pongServe(state.copy(bottomScore = state.bottomScore + 1), random)
    return state.copy(ballX = x, ballY = y, velX = vx.coerceIn(-0.02f, 0.02f), velY = vy.coerceIn(-0.025f, 0.025f))
}

/** Bot slides its paddle toward the ball with a capped speed. */
internal fun pongBotMove(state: PongState, maxStep: Float): PongState {
    val delta = (state.ballX - state.topPaddle).coerceIn(-maxStep, maxStep)
    return state.copy(topPaddle = (state.topPaddle + delta).coerceIn(PONG_PADDLE_W / 2, 1f - PONG_PADDLE_W / 2))
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PongScreen(
    mode: PongMode = PongMode.VsBot,
    onBack: () -> Unit,
    onFinished: (playerWon: Boolean) -> Unit = {}
) {
    var state by remember(mode) { mutableStateOf(pongServe(PongState())) }
    var running by remember(mode) { mutableStateOf(false) }
    var reported by remember(mode) { mutableStateOf(false) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(running, mode) {
        while (running) {
            delay(16)
            var next = pongTick(state, mode.speed)
            if (mode == PongMode.VsBot) next = pongBotMove(next, mode.botStep)
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
        state = pongServe(PongState())
        running = false
        reported = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pong Duel • ${mode.label}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
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
                    "${if (mode == PongMode.VsBot) "Bot" else "Top"} ${state.topScore} — ${state.bottomScore} ${if (mode == PongMode.VsBot) "You" else "Bottom"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                when {
                    state.winner == 1 -> Text("Bottom wins! 🏆", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    state.winner == 2 -> Text("Top wins!", color = Color.Red, fontWeight = FontWeight.Bold)
                    !running -> Text("Drag to start", style = MaterialTheme.typography.bodySmall)
                    else -> {}
                }
                Button(onClick = ::reset) { Text("New Match") }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF102027), RoundedCornerShape(12.dp))
                    .onSizeChanged { boardSize = it }
                    .pointerInput(mode) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            if (state.winner != null || boardSize.width == 0) return@detectDragGestures
                            running = true
                            val nx = (change.position.x / boardSize.width)
                                .coerceIn(PONG_PADDLE_W / 2, 1f - PONG_PADDLE_W / 2)
                            val isTopHalf = change.position.y < boardSize.height / 2
                            state = if (mode == PongMode.TwoPlayer && isTopHalf) {
                                state.copy(topPaddle = nx)
                            } else {
                                state.copy(bottomPaddle = nx)
                            }
                        }
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    // Center line
                    drawRect(Color.White.copy(alpha = 0.15f), topLeft = Offset(0f, h / 2 - 1), size = Size(w, 2f))
                    // Paddles
                    drawRect(
                        Color(0xFF4FC3F7),
                        topLeft = Offset((state.bottomPaddle - PONG_PADDLE_W / 2) * w, 0.96f * h),
                        size = Size(PONG_PADDLE_W * w, PONG_PADDLE_H * h)
                    )
                    drawRect(
                        Color(0xFFF06292),
                        topLeft = Offset((state.topPaddle - PONG_PADDLE_W / 2) * w, (0.04f - PONG_PADDLE_H) * h),
                        size = Size(PONG_PADDLE_W * w, PONG_PADDLE_H * h)
                    )
                    // Ball
                    drawCircle(Color.White, radius = PONG_BALL_R * h, center = Offset(state.ballX * w, state.ballY * h))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (mode == PongMode.TwoPlayer)
                    "Two players, one screen: each drags on their own half. First to $PONG_WIN_SCORE."
                else
                    "Drag anywhere to move your paddle. Edges of the paddle add spin. First to $PONG_WIN_SCORE wins.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
