package net.sclan.minigames.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — falling-sand cellular automaton
// Grid stored as IntArray, -1 = empty, otherwise palette index.
// ---------------------------------------------------------------------------

internal const val SAND_COLS = 48
internal const val SAND_ROWS = 64

internal fun emptySand(): IntArray = IntArray(SAND_COLS * SAND_ROWS) { -1 }

internal fun sandIndex(x: Int, y: Int): Int = y * SAND_COLS + x

/** One physics step: grains fall straight down, else slide diagonally. */
internal fun stepSand(grid: IntArray, random: Random = Random.Default): IntArray {
    val next = grid.copyOf()
    for (y in SAND_ROWS - 2 downTo 0) {
        for (x in 0 until SAND_COLS) {
            val i = sandIndex(x, y)
            val grain = next[i]
            if (grain < 0) continue
            val below = sandIndex(x, y + 1)
            if (next[below] < 0) {
                next[below] = grain
                next[i] = -1
            } else {
                val dir = if (random.nextBoolean()) 1 else -1
                val d1x = x + dir
                val d2x = x - dir
                if (d1x in 0 until SAND_COLS && next[sandIndex(d1x, y + 1)] < 0) {
                    next[sandIndex(d1x, y + 1)] = grain
                    next[i] = -1
                } else if (d2x in 0 until SAND_COLS && next[sandIndex(d2x, y + 1)] < 0) {
                    next[sandIndex(d2x, y + 1)] = grain
                    next[i] = -1
                }
            }
        }
    }
    return next
}

/** Drops a brush-sized blob of grains at (cx, cy). Returns grains added. */
internal fun dropSand(grid: IntArray, cx: Int, cy: Int, brush: Int, colorIdx: Int): Int {
    var added = 0
    for (dy in -brush..brush) for (dx in -brush..brush) {
        val x = cx + dx
        val y = cy + dy
        if (x in 0 until SAND_COLS && y in 0 until SAND_ROWS) {
            val i = sandIndex(x, y)
            if (grid[i] < 0) {
                grid[i] = colorIdx
                added++
            }
        }
    }
    return added
}

internal val sandPalette = listOf(
    Color(0xFFFFD54F), Color(0xFFFFB74D), Color(0xFFFF8A65), Color(0xFFF06292),
    Color(0xFFBA68C8), Color(0xFF7986CB), Color(0xFF4FC3F7), Color(0xFF4DB6AC),
    Color(0xFF81C784), Color(0xFFAED581)
)

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandFallScreen(
    brush: SandBrush = SandBrush.Normal,
    onBack: () -> Unit
) {
    var grid by remember(brush) { mutableStateOf(emptySand()) }
    var poured by remember(brush) { mutableIntStateOf(0) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(brush) {
        while (true) {
            delay(45)
            grid = stepSand(grid)
        }
    }

    fun pourAt(px: Float, py: Float) {
        if (boardSize.width == 0) return
        val cx = (px / boardSize.width * SAND_COLS).toInt().coerceIn(0, SAND_COLS - 1)
        val cy = (py / boardSize.height * SAND_ROWS).toInt().coerceIn(0, SAND_ROWS - 1)
        val colorIdx = (poured / 60) % sandPalette.size
        val work = grid.copyOf()
        poured += dropSand(work, cx, cy, brush.radius, colorIdx)
        grid = work
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sand Fall • ${brush.label}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Grains: $poured", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { grid = emptySand(); poured = 0 }) { Text("Clear") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF162026), RoundedCornerShape(12.dp))
                    .onSizeChanged { boardSize = it }
                    .pointerInput(brush) {
                        detectTapGestures(onTap = { pos -> pourAt(pos.x, pos.y) })
                    }
                    .pointerInput(brush, boardSize) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            pourAt(change.position.x, change.position.y)
                        }
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cw = this.size.width / SAND_COLS
                    val ch = this.size.height / SAND_ROWS
                    for (y in 0 until SAND_ROWS) {
                        for (x in 0 until SAND_COLS) {
                            val grain = grid[sandIndex(x, y)]
                            if (grain >= 0) {
                                drawRect(
                                    sandPalette[grain % sandPalette.size],
                                    topLeft = Offset(x * cw, y * ch),
                                    size = Size(cw, ch)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Tap or drag to pour sand. It piles, slides, and settles. Colors shift as you pour. No goal — just vibes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
