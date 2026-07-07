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
// Pure game logic (SameGame-style collapse; -1 = empty)
// ---------------------------------------------------------------------------

internal fun generateBlockGrid(rows: Int, cols: Int, colors: Int, random: Random = Random.Default): List<List<Int>> =
    List(rows) { List(cols) { random.nextInt(colors) } }

internal fun blockGroup(grid: List<List<Int>>, row: Int, col: Int): Set<Pair<Int, Int>> {
    val color = grid[row][col]
    if (color < 0) return emptySet()
    val rows = grid.size
    val cols = grid[0].size
    val group = mutableSetOf(row to col)
    val queue = ArrayDeque(listOf(row to col))
    while (queue.isNotEmpty()) {
        val (r, c) = queue.removeFirst()
        listOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1).forEach { (nr, nc) ->
            if (nr in 0 until rows && nc in 0 until cols && (nr to nc) !in group && grid[nr][nc] == color) {
                group.add(nr to nc)
                queue.add(nr to nc)
            }
        }
    }
    return group
}

/** Removes a group, applies gravity, and collapses empty columns leftward. */
internal fun removeBlockGroup(grid: List<List<Int>>, group: Set<Pair<Int, Int>>): List<List<Int>> {
    val rows = grid.size
    val cols = grid[0].size
    val work = grid.map { it.toMutableList() }
    group.forEach { (r, c) -> work[r][c] = -1 }
    // Gravity per column
    for (c in 0 until cols) {
        val stack = (0 until rows).map { work[it][c] }.filter { it >= 0 }
        val padded = List(rows - stack.size) { -1 } + stack
        for (r in 0 until rows) work[r][c] = padded[r]
    }
    // Collapse empty columns to the left
    val keptCols = (0 until cols).filter { c -> (0 until rows).any { work[it][c] >= 0 } }
    val result = List(rows) { r ->
        MutableList(cols) { -1 }.also { rowOut ->
            keptCols.forEachIndexed { newC, oldC -> rowOut[newC] = work[r][oldC] }
        }
    }
    return result
}

internal fun blockScoreFor(groupSize: Int): Int = (groupSize - 1) * (groupSize - 1)

internal fun hasBlockMoves(grid: List<List<Int>>): Boolean {
    for (r in grid.indices) for (c in grid[0].indices) {
        if (grid[r][c] >= 0 && blockGroup(grid, r, c).size >= 2) return true
    }
    return false
}

internal fun blocksRemaining(grid: List<List<Int>>): Int =
    grid.sumOf { row -> row.count { it >= 0 } }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorBlocksScreen(
    difficulty: ColorBlocksDifficulty = ColorBlocksDifficulty.Normal,
    onBack: () -> Unit,
    onFinished: (score: Int) -> Unit = {}
) {
    var grid by remember(difficulty) { mutableStateOf(generateBlockGrid(difficulty.rows, difficulty.cols, difficulty.colors)) }
    var score by remember(difficulty) { mutableIntStateOf(0) }
    var over by remember(difficulty) { mutableStateOf(false) }
    var reported by remember(difficulty) { mutableStateOf(false) }

    fun reset() {
        grid = generateBlockGrid(difficulty.rows, difficulty.cols, difficulty.colors)
        score = 0
        over = false
        reported = false
    }

    fun tap(r: Int, c: Int) {
        if (over || grid[r][c] < 0) return
        val group = blockGroup(grid, r, c)
        if (group.size < 2) return
        score += blockScoreFor(group.size)
        grid = removeBlockGroup(grid, group)
        if (!hasBlockMoves(grid)) {
            if (blocksRemaining(grid) == 0) score += 100 // clear bonus
            over = true
            if (!reported) {
                reported = true
                onFinished(score)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Color Blocks • ${difficulty.label}") },
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
                Text("Score: $score", style = MaterialTheme.typography.titleMedium)
                if (over) Text(
                    if (blocksRemaining(grid) == 0) "Board cleared! +100" else "No moves left",
                    color = if (blocksRemaining(grid) == 0) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = ::reset) { Text("New Board") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(difficulty.cols.toFloat() / difficulty.rows)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(3.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (r in grid.indices) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in grid[0].indices) {
                                val colorIdx = grid[r][c]
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.dp)
                                        .background(
                                            if (colorIdx >= 0) sortColors[colorIdx % sortColors.size] else Color.Transparent,
                                            RoundedCornerShape(3.dp)
                                        )
                                        .clickable(enabled = colorIdx >= 0) { tap(r, c) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tap a group of 2+ touching same-color blocks to clear it. Bigger groups score much more. Clear the whole board for a bonus!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
