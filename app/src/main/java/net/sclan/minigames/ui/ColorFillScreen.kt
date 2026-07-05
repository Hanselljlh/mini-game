package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic (Flood-It)
// ---------------------------------------------------------------------------

internal fun generateFloodGrid(size: Int, colors: Int, random: Random = Random.Default): List<List<Int>> =
    List(size) { List(size) { random.nextInt(colors) } }

/** Flood-fills from the top-left with [newColor]; returns the new grid. */
internal fun floodFill(grid: List<List<Int>>, newColor: Int): List<List<Int>> {
    val oldColor = grid[0][0]
    if (oldColor == newColor) return grid
    val size = grid.size
    val result = grid.map { it.toMutableList() }
    val queue = ArrayDeque(listOf(0 to 0))
    val seen = mutableSetOf(0 to 0)
    while (queue.isNotEmpty()) {
        val (r, c) = queue.removeFirst()
        if (result[r][c] != oldColor) continue
        result[r][c] = newColor
        listOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1).forEach { (nr, nc) ->
            if (nr in 0 until size && nc in 0 until size && (nr to nc) !in seen && result[nr][nc] == oldColor) {
                seen.add(nr to nc)
                queue.add(nr to nc)
            }
        }
    }
    return result
}

internal fun floodComplete(grid: List<List<Int>>): Boolean {
    val first = grid[0][0]
    return grid.all { row -> row.all { it == first } }
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorFillScreen(
    difficulty: ColorFillDifficulty = ColorFillDifficulty.Normal,
    onBack: () -> Unit,
    onWin: (movesLeft: Int) -> Unit = {}
) {
    var grid by remember(difficulty) { mutableStateOf(generateFloodGrid(difficulty.gridSize, difficulty.colors)) }
    var movesUsed by remember(difficulty) { mutableIntStateOf(0) }
    var won by remember(difficulty) { mutableStateOf(false) }
    var lost by remember(difficulty) { mutableStateOf(false) }

    fun reset() {
        grid = generateFloodGrid(difficulty.gridSize, difficulty.colors)
        movesUsed = 0
        won = false
        lost = false
    }

    fun pick(color: Int) {
        if (won || lost) return
        val next = floodFill(grid, color)
        if (next == grid) return
        grid = next
        movesUsed++
        when {
            floodComplete(next) -> {
                won = true
                onWin(difficulty.moveLimit - movesUsed)
            }
            movesUsed >= difficulty.moveLimit -> lost = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-Color Fill • ${difficulty.label}") },
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
                Text("Moves: $movesUsed/${difficulty.moveLimit}", style = MaterialTheme.typography.titleMedium)
                when {
                    won -> Text("Filled! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    lost -> Text("Out of moves!", color = Color.Red, fontWeight = FontWeight.Bold)
                    else -> {}
                }
                Button(onClick = ::reset) { Text("New Board") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    grid.forEach { row ->
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            row.forEach { colorIdx ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(sortColors[colorIdx % sortColors.size])
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (color in 0 until difficulty.colors) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(sortColors[color % sortColors.size], CircleShape)
                            .clickable { pick(color) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tap a color to flood from the top-left corner. Turn the whole board one color before moves run out.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
