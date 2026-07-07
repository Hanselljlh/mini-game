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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal enum class MazeDir(val dx: Int, val dy: Int) {
    N(0, -1), S(0, 1), E(1, 0), W(-1, 0);

    val opposite: MazeDir get() = when (this) { N -> S; S -> N; E -> W; W -> E }
}

/** open[cell] = set of directions with open passages (no wall). */
internal data class Maze(val size: Int, val open: Map<Pair<Int, Int>, Set<MazeDir>>)

/** Iterative DFS ("recursive backtracker") — every cell reachable, no loops. */
internal fun generateMaze(size: Int, random: Random = Random.Default): Maze {
    val open = mutableMapOf<Pair<Int, Int>, MutableSet<MazeDir>>()
    val visited = mutableSetOf<Pair<Int, Int>>()
    val stack = ArrayDeque<Pair<Int, Int>>()
    var current = 0 to 0
    visited.add(current)
    stack.addLast(current)

    while (stack.isNotEmpty()) {
        current = stack.last()
        val (x, y) = current
        val neighbors = MazeDir.entries.mapNotNull { dir ->
            val nx = x + dir.dx
            val ny = y + dir.dy
            if (nx in 0 until size && ny in 0 until size && (nx to ny) !in visited) dir to (nx to ny) else null
        }
        if (neighbors.isEmpty()) {
            stack.removeLast()
        } else {
            val (dir, next) = neighbors[random.nextInt(neighbors.size)]
            open.getOrPut(current) { mutableSetOf() }.add(dir)
            open.getOrPut(next) { mutableSetOf() }.add(dir.opposite)
            visited.add(next)
            stack.addLast(next)
        }
    }
    return Maze(size, open)
}

/** Slide from [from] in [dir] until a wall or a junction (Tomb-of-the-Mask style stops at walls only). */
internal fun slideMaze(maze: Maze, from: Pair<Int, Int>, dir: MazeDir): Pair<Int, Int> {
    var pos = from
    while (dir in (maze.open[pos] ?: emptySet())) {
        pos = (pos.first + dir.dx) to (pos.second + dir.dy)
    }
    return pos
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MazeRunnerScreen(
    size: MazeSize = MazeSize.Medium,
    onBack: () -> Unit,
    onWin: (timeSecs: Long) -> Unit = {}
) {
    var maze by remember(size) { mutableStateOf(generateMaze(size.size)) }
    var player by remember(size) { mutableStateOf(0 to 0) }
    var startTime by remember(size) { mutableLongStateOf(0L) }
    var won by remember(size) { mutableStateOf(false) }
    val exit = (size.size - 1) to (size.size - 1)

    fun reset() {
        maze = generateMaze(size.size)
        player = 0 to 0
        startTime = 0L
        won = false
    }

    fun move(dir: MazeDir) {
        if (won) return
        if (startTime == 0L) startTime = System.currentTimeMillis()
        player = slideMaze(maze, player, dir)
        if (player == exit) {
            won = true
            onWin((System.currentTimeMillis() - startTime) / 1000L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maze Runner • ${size.label}") },
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
                if (won) Text("Escaped! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                else Text("Reach the ★", style = MaterialTheme.typography.titleMedium)
                Button(onClick = ::reset) { Text("New Maze") }
            }

            Spacer(Modifier.height(12.dp))

            val wallColor = MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .pointerInput(size, won) {
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
                                when {
                                    abs(dx) > abs(dy) && abs(dx) > threshold -> move(if (dx > 0) MazeDir.E else MazeDir.W)
                                    abs(dy) > threshold -> move(if (dy > 0) MazeDir.S else MazeDir.N)
                                }
                            }
                        )
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val n = maze.size
                    val cell = this.size.width / n
                    val stroke = (cell * 0.12f).coerceAtLeast(3f)

                    // Walls: draw the side when the passage is NOT open.
                    for (x in 0 until n) for (y in 0 until n) {
                        val openDirs = maze.open[x to y] ?: emptySet()
                        val left = x * cell
                        val top = y * cell
                        if (MazeDir.N !in openDirs) drawLine(wallColor, Offset(left, top), Offset(left + cell, top), stroke, StrokeCap.Round)
                        if (MazeDir.W !in openDirs) drawLine(wallColor, Offset(left, top), Offset(left, top + cell), stroke, StrokeCap.Round)
                        if (y == n - 1 && MazeDir.S !in openDirs) drawLine(wallColor, Offset(left, top + cell), Offset(left + cell, top + cell), stroke, StrokeCap.Round)
                        if (x == n - 1 && MazeDir.E !in openDirs) drawLine(wallColor, Offset(left + cell, top), Offset(left + cell, top + cell), stroke, StrokeCap.Round)
                    }

                    // Exit
                    drawCircle(
                        Color(0xFFFFB300),
                        radius = cell * 0.28f,
                        center = Offset((n - 1 + 0.5f) * cell, (n - 1 + 0.5f) * cell)
                    )
                    // Player
                    drawCircle(
                        Color(0xFF1E88E5),
                        radius = cell * 0.3f,
                        center = Offset((player.first + 0.5f) * cell, (player.second + 0.5f) * cell)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Swipe to slide — you glide until you hit a wall. Get from the top-left to the gold dot.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
