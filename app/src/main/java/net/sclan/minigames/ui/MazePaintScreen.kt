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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// ---------------------------------------------------------------------------
// Pure game logic (reuses Maze/MazeDir/generateMaze from MazeRunnerScreen.kt)
// ---------------------------------------------------------------------------

/** Slide like Maze Runner, but return every cell passed through (for painting). */
internal fun slidePaintMaze(maze: Maze, from: Pair<Int, Int>, dir: MazeDir): List<Pair<Int, Int>> {
    val path = mutableListOf(from)
    var pos = from
    while (dir in (maze.open[pos] ?: emptySet())) {
        pos = (pos.first + dir.dx) to (pos.second + dir.dy)
        path.add(pos)
    }
    return path
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MazePaintScreen(
    size: MazeSize = MazeSize.Medium,
    onBack: () -> Unit,
    onWin: (swipes: Int) -> Unit = {}
) {
    var maze by remember(size) { mutableStateOf(generateMaze(size.size)) }
    var player by remember(size) { mutableStateOf(0 to 0) }
    var painted by remember(size) { mutableStateOf(setOf(0 to 0)) }
    var swipes by remember(size) { mutableIntStateOf(0) }
    var won by remember(size) { mutableStateOf(false) }
    val total = size.size * size.size

    fun reset() {
        maze = generateMaze(size.size)
        player = 0 to 0
        painted = setOf(0 to 0)
        swipes = 0
        won = false
    }

    fun move(dir: MazeDir) {
        if (won) return
        val path = slidePaintMaze(maze, player, dir)
        if (path.size <= 1) return
        swipes++
        player = path.last()
        painted = painted + path
        if (painted.size == total) {
            won = true
            onWin(swipes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maze Paint • ${size.label}") },
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
                Text("Painted ${painted.size}/$total", style = MaterialTheme.typography.titleMedium)
                if (won) Text("All painted! 🎨", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
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

                    // Painted cells
                    painted.forEach { (x, y) ->
                        drawRect(
                            Color(0xFF7E57C2).copy(alpha = 0.55f),
                            topLeft = Offset(x * cell, y * cell),
                            size = Size(cell, cell)
                        )
                    }

                    // Walls
                    for (x in 0 until n) for (y in 0 until n) {
                        val openDirs = maze.open[x to y] ?: emptySet()
                        val left = x * cell
                        val top = y * cell
                        if (MazeDir.N !in openDirs) drawLine(wallColor, Offset(left, top), Offset(left + cell, top), stroke, StrokeCap.Round)
                        if (MazeDir.W !in openDirs) drawLine(wallColor, Offset(left, top), Offset(left, top + cell), stroke, StrokeCap.Round)
                        if (y == n - 1 && MazeDir.S !in openDirs) drawLine(wallColor, Offset(left, top + cell), Offset(left + cell, top + cell), stroke, StrokeCap.Round)
                        if (x == n - 1 && MazeDir.E !in openDirs) drawLine(wallColor, Offset(left + cell, top), Offset(left + cell, top + cell), stroke, StrokeCap.Round)
                    }

                    // Player
                    drawCircle(
                        Color(0xFF5E35B1),
                        radius = cell * 0.3f,
                        center = Offset((player.first + 0.5f) * cell, (player.second + 0.5f) * cell)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Swipe to slide and paint every square you pass. Cover the whole maze in as few swipes as you can.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
