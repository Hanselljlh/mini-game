package net.sclan.minigames.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — normalized coordinates: x, y in [0,1]
// ---------------------------------------------------------------------------

internal data class FlappyPipe(val x: Float, val gapCenter: Float)

internal data class FlappyState(
    val birdY: Float = 0.5f,
    val velocity: Float = 0f,
    val pipes: List<FlappyPipe> = emptyList(),
    val distance: Float = 0f,
    val score: Int = 0,
    val alive: Boolean = true
)

internal const val FLAPPY_BIRD_X = 0.28f
internal const val FLAPPY_BIRD_R = 0.030f
internal const val FLAPPY_PIPE_W = 0.10f
internal const val FLAPPY_GRAVITY = 0.0022f
internal const val FLAPPY_JUMP_V = -0.030f
internal const val FLAPPY_PIPE_SPACING = 0.55f

internal fun flappyTick(state: FlappyState, speed: Float, gap: Float, random: Random = Random.Default): FlappyState {
    if (!state.alive) return state
    val velocity = state.velocity + FLAPPY_GRAVITY
    val birdY = state.birdY + velocity

    var pipes = state.pipes.map { it.copy(x = it.x - speed) }
    var score = state.score
    // Score pipes that just crossed the bird
    state.pipes.forEach { old ->
        if (old.x >= FLAPPY_BIRD_X && old.x - speed < FLAPPY_BIRD_X) score++
    }
    pipes = pipes.filter { it.x > -FLAPPY_PIPE_W }
    val rightmost = pipes.maxOfOrNull { it.x }
    if (rightmost == null || rightmost <= 1.05f - FLAPPY_PIPE_SPACING) {
        pipes = pipes + FlappyPipe(x = 1.05f, gapCenter = 0.2f + random.nextFloat() * 0.6f)
    }

    val hitGround = birdY + FLAPPY_BIRD_R >= 1f || birdY - FLAPPY_BIRD_R <= 0f
    val hitPipe = pipes.any { pipe ->
        val inX = FLAPPY_BIRD_X + FLAPPY_BIRD_R > pipe.x && FLAPPY_BIRD_X - FLAPPY_BIRD_R < pipe.x + FLAPPY_PIPE_W
        val inGap = birdY - FLAPPY_BIRD_R > pipe.gapCenter - gap / 2 && birdY + FLAPPY_BIRD_R < pipe.gapCenter + gap / 2
        inX && !inGap
    }

    return state.copy(
        birdY = birdY,
        velocity = velocity,
        pipes = pipes,
        distance = state.distance + speed,
        score = score,
        alive = !(hitGround || hitPipe)
    )
}

internal fun flappyJump(state: FlappyState): FlappyState =
    if (state.alive) state.copy(velocity = FLAPPY_JUMP_V) else state

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlappyJumpScreen(
    difficulty: FlappyDifficulty = FlappyDifficulty.Normal,
    onBack: () -> Unit,
    onGameOver: (score: Int) -> Unit = {}
) {
    var state by remember(difficulty) { mutableStateOf(FlappyState()) }
    var running by remember(difficulty) { mutableStateOf(false) }
    var reported by remember(difficulty) { mutableStateOf(false) }

    LaunchedEffect(running, difficulty) {
        while (running) {
            delay(24)
            state = flappyTick(state, difficulty.speed, difficulty.gap)
            if (!state.alive) {
                running = false
                if (!reported) {
                    reported = true
                    onGameOver(state.score)
                }
            }
        }
    }

    fun tap() {
        if (!state.alive) return
        if (!running) running = true
        state = flappyJump(state)
    }

    fun reset() {
        state = FlappyState()
        running = false
        reported = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flappy Jump • ${difficulty.label}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Score: ${state.score}", style = MaterialTheme.typography.titleMedium)
                if (!state.alive) Text("Crashed!", color = Color.Red, fontWeight = FontWeight.Bold)
                else if (!running) Text("Tap to flap", style = MaterialTheme.typography.bodySmall)
                Button(onClick = ::reset) { Text("New Run") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF81D4FA), RoundedCornerShape(12.dp))
                    .clickable { tap() }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    // Pipes
                    state.pipes.forEach { pipe ->
                        val gapTop = (pipe.gapCenter - difficulty.gap / 2) * h
                        val gapBottom = (pipe.gapCenter + difficulty.gap / 2) * h
                        val px = pipe.x * w
                        val pw = FLAPPY_PIPE_W * w
                        drawRect(Color(0xFF388E3C), topLeft = Offset(px, 0f), size = Size(pw, gapTop))
                        drawRect(Color(0xFF388E3C), topLeft = Offset(px, gapBottom), size = Size(pw, h - gapBottom))
                    }
                    // Bird
                    drawCircle(
                        Color(0xFFFFC107),
                        radius = FLAPPY_BIRD_R * h,
                        center = Offset(FLAPPY_BIRD_X * w, state.birdY * h)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Tap to flap upward. Slip through the gaps — one touch and it's over.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
