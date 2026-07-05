package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal const val STACK_WIDTH = 12   // board width in cells
internal const val STACK_MAX_LAYERS = 12

internal data class StackLayer(val start: Int, val width: Int) {
    val end: Int get() = start + width // exclusive
}

internal data class StackState(
    val layers: List<StackLayer> = listOf(StackLayer(4, 4)), // fixed base
    val moving: StackLayer = StackLayer(0, 4),
    val movingRight: Boolean = true,
    val gameOver: Boolean = false,
    val won: Boolean = false
) {
    val score: Int get() = layers.size - 1 // base doesn't count
}

internal fun tickStack(state: StackState): StackState {
    if (state.gameOver || state.won) return state
    val m = state.moving
    return if (state.movingRight) {
        if (m.end >= STACK_WIDTH) state.copy(movingRight = false, moving = m.copy(start = m.start - 1))
        else state.copy(moving = m.copy(start = m.start + 1))
    } else {
        if (m.start <= 0) state.copy(movingRight = true, moving = m.copy(start = m.start + 1))
        else state.copy(moving = m.copy(start = m.start - 1))
    }
}

/** Drop the moving layer onto the top of the stack, trimming to the overlap. */
internal fun placeStack(state: StackState): StackState {
    if (state.gameOver || state.won) return state
    val top = state.layers.last()
    val start = maxOf(top.start, state.moving.start)
    val end = minOf(top.end, state.moving.end)
    if (end <= start) return state.copy(gameOver = true)
    val placed = StackLayer(start, end - start)
    val layers = state.layers + placed
    return if (layers.size >= STACK_MAX_LAYERS + 1) { // +1 for base
        state.copy(layers = layers, won = true)
    } else {
        state.copy(layers = layers, moving = StackLayer(0, placed.width), movingRight = true)
    }
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val layerColors = listOf(
    Color(0xFF4FC3F7), Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFFAED581),
    Color(0xFFFFD54F), Color(0xFFFFB74D), Color(0xFFFF8A65), Color(0xFFF06292),
    Color(0xFFBA68C8), Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFF64B5F6)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimingStackScreen(
    speed: TimingStackSpeed = TimingStackSpeed.Normal,
    onBack: () -> Unit,
    onFinished: (layers: Int) -> Unit = {}
) {
    var state by remember(speed) { mutableStateOf(StackState()) }
    var running by remember(speed) { mutableStateOf(false) }
    var reported by remember(speed) { mutableStateOf(false) }

    LaunchedEffect(running, speed) {
        while (running) {
            delay(speed.tickMs)
            state = tickStack(state)
        }
    }

    fun finishIfNeeded() {
        if ((state.gameOver || state.won) && !reported) {
            reported = true
            running = false
            onFinished(state.score)
        }
    }

    fun tap() {
        if (state.gameOver || state.won) return
        if (!running) {
            running = true
            return
        }
        state = placeStack(state)
        finishIfNeeded()
    }

    fun reset() {
        state = StackState()
        running = false
        reported = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timing Stack • ${speed.label}") },
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
                Text("Layers: ${state.score}/$STACK_MAX_LAYERS", style = MaterialTheme.typography.titleMedium)
                when {
                    state.won -> Text("Perfect tower!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    state.gameOver -> Text("Missed!", color = Color.Red, fontWeight = FontWeight.Bold)
                    !running -> Text("Tap to start", style = MaterialTheme.typography.bodySmall)
                    else -> {}
                }
                Button(onClick = ::reset) { Text("New Tower") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF263238), RoundedCornerShape(12.dp))
                    .padding(6.dp)
                    .clickable { tap() }
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Render rows top-down: moving layer sits just above the stack.
                    val stackHeight = state.layers.size
                    val movingRow = STACK_MAX_LAYERS - stackHeight // 0-based from top region
                    for (row in 0 until STACK_MAX_LAYERS + 1) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            val layerIndex = STACK_MAX_LAYERS - row // which stack slot this row shows
                            val layer: StackLayer? = when {
                                layerIndex < stackHeight -> state.layers[layerIndex]
                                row == movingRow && !state.gameOver && !state.won && running -> state.moving
                                else -> null
                            }
                            for (x in 0 until STACK_WIDTH) {
                                val filled = layer != null && x >= layer.start && x < layer.end
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.dp)
                                        .background(
                                            if (filled) layerColors[layerIndex.coerceIn(0, layerColors.lastIndex)]
                                            else Color.Transparent,
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Tap anywhere to drop the sliding block. Only the overlap survives — reach $STACK_MAX_LAYERS layers!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
