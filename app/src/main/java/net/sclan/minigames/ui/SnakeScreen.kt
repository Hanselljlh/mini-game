package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal const val SNAKE_COLS = 15
internal const val SNAKE_ROWS = 15

internal enum class SnakeDir(val dx: Int, val dy: Int) {
    Up(0, -1), Down(0, 1), Left(-1, 0), Right(1, 0);

    val opposite: SnakeDir
        get() = when (this) {
            Up -> Down; Down -> Up; Left -> Right; Right -> Left
        }
}

internal data class SnakeState(
    val body: List<Pair<Int, Int>>, // head first
    val dir: SnakeDir,
    val food: Pair<Int, Int>,
    val score: Int = 0,
    val alive: Boolean = true
)

internal fun newSnakeState(random: Random = Random.Default): SnakeState {
    val start = listOf(7 to 7, 6 to 7, 5 to 7)
    return SnakeState(body = start, dir = SnakeDir.Right, food = spawnFood(start, random))
}

internal fun spawnFood(body: List<Pair<Int, Int>>, random: Random = Random.Default): Pair<Int, Int> {
    val empties = (0 until SNAKE_COLS).flatMap { x -> (0 until SNAKE_ROWS).map { y -> x to y } }
        .filter { it !in body }
    return if (empties.isEmpty()) -1 to -1 else empties[random.nextInt(empties.size)]
}

/** Turn request is ignored when it would reverse into the snake's own neck. */
internal fun turnSnake(state: SnakeState, dir: SnakeDir): SnakeState =
    if (dir == state.dir.opposite) state else state.copy(dir = dir)

internal fun stepSnake(state: SnakeState, random: Random = Random.Default): SnakeState {
    if (!state.alive) return state
    val head = state.body.first()
    val next = (head.first + state.dir.dx) to (head.second + state.dir.dy)
    val hitsWall = next.first !in 0 until SNAKE_COLS || next.second !in 0 until SNAKE_ROWS
    // Moving into the current tail tip is legal because the tail moves away this tick.
    val bodyWithoutTail = state.body.dropLast(1)
    if (hitsWall || next in bodyWithoutTail) return state.copy(alive = false)
    return if (next == state.food) {
        val grown = listOf(next) + state.body
        state.copy(body = grown, score = state.score + 1, food = spawnFood(grown, random))
    } else {
        state.copy(body = listOf(next) + bodyWithoutTail)
    }
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeScreen(
    difficulty: SnakeDifficulty = SnakeDifficulty.Normal,
    onBack: () -> Unit,
    onGameOver: (score: Int) -> Unit = {}
) {
    var state by remember(difficulty) { mutableStateOf(newSnakeState()) }
    var started by remember(difficulty) { mutableStateOf(false) }
    var reported by remember(difficulty) { mutableStateOf(false) }

    LaunchedEffect(started, difficulty) {
        while (started) {
            delay(difficulty.tickMs)
            if (state.alive) {
                state = stepSnake(state)
                if (!state.alive && !reported) {
                    reported = true
                    onGameOver(state.score)
                }
            }
        }
    }

    fun reset() {
        state = newSnakeState()
        started = false
        reported = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snake • ${difficulty.label}") },
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
                if (!state.alive) Text("Game Over!", color = Color.Red, fontWeight = FontWeight.Bold)
                else if (!started) Text("Swipe to steer", style = MaterialTheme.typography.bodySmall)
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFF1B2B1B), RoundedCornerShape(8.dp))
                    .padding(4.dp)
                    .pointerInput(difficulty) {
                        var dx = 0f
                        var dy = 0f
                        detectDragGestures(
                            onDragStart = { dx = 0f; dy = 0f },
                            onDrag = { change, amount ->
                                change.consume()
                                dx += amount.x
                                dy += amount.y
                            },
                            onDragEnd = {
                                val threshold = 30f
                                val dir = when {
                                    abs(dx) > abs(dy) && abs(dx) > threshold ->
                                        if (dx > 0) SnakeDir.Right else SnakeDir.Left
                                    abs(dy) > threshold ->
                                        if (dy > 0) SnakeDir.Down else SnakeDir.Up
                                    else -> null
                                }
                                if (dir != null) {
                                    state = turnSnake(state, dir)
                                    started = true
                                }
                            }
                        )
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (y in 0 until SNAKE_ROWS) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (x in 0 until SNAKE_COLS) {
                                val cell = x to y
                                val color = when {
                                    cell == state.body.firstOrNull() -> Color(0xFF8BC34A)
                                    cell in state.body -> Color(0xFF4CAF50)
                                    cell == state.food -> Color(0xFFFF7043)
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.dp)
                                        .background(color, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                if (started) "Swipe anywhere on the board to change direction."
                else "Swipe on the board to start moving.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
