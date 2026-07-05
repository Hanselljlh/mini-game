package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — Number Connect (Hidato-lite):
// A hidden Hamiltonian path (4-directional) numbers the grid 1..N.
// Checkpoints are revealed; the player retraces the path from 1.
// ---------------------------------------------------------------------------

/** Generates a Hamiltonian path over an n×n grid using Warnsdorff + backtracking. */
internal fun generateNumberPath(n: Int, random: Random = Random.Default): List<Pair<Int, Int>> {
    val total = n * n
    val dirs = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)

    fun neighbors(cell: Pair<Int, Int>, visited: Set<Pair<Int, Int>>): List<Pair<Int, Int>> =
        dirs.map { (dr, dc) -> (cell.first + dr) to (cell.second + dc) }
            .filter { (r, c) -> r in 0 until n && c in 0 until n && (r to c) !in visited }

    val start = random.nextInt(n) to random.nextInt(n)
    val path = mutableListOf(start)
    val visited = mutableSetOf(start)

    fun extend(): Boolean {
        if (path.size == total) return true
        val current = path.last()
        // Warnsdorff: prefer neighbors with fewest onward moves; shuffle equal ranks
        val options = neighbors(current, visited)
            .sortedBy { nb -> neighbors(nb, visited + nb).size * 10 + random.nextInt(10) }
        for (next in options) {
            path.add(next)
            visited.add(next)
            if (extend()) return true
            path.removeAt(path.size - 1)
            visited.remove(next)
        }
        return false
    }

    return if (extend()) path.toList() else generateNumberPath(n, random)
}

/** Which step numbers (1-based) are revealed: 1, N, and every [every]-th. */
internal fun revealedSteps(total: Int, every: Int): Set<Int> =
    (1..total).filter { it == 1 || it == total || it % every == 0 }.toSet()

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberConnectScreen(
    difficulty: NumberConnectDifficulty = NumberConnectDifficulty.Normal,
    onBack: () -> Unit,
    onWin: () -> Unit = {}
) {
    val n = difficulty.gridSize
    val total = n * n
    var solution by remember(difficulty) { mutableStateOf(generateNumberPath(n)) }
    var progress by remember(difficulty) { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var won by remember(difficulty) { mutableStateOf(false) }

    val revealed = remember(difficulty, solution) { revealedSteps(total, difficulty.revealEvery) }
    val stepOf = remember(solution) { solution.mapIndexed { i, cell -> cell to (i + 1) }.toMap() }

    fun newPuzzle() {
        solution = generateNumberPath(n)
        progress = emptyList()
        won = false
    }

    fun tap(cell: Pair<Int, Int>) {
        if (won) return
        val step = stepOf[cell] ?: return
        when {
            progress.isEmpty() -> if (step == 1) progress = listOf(cell)
            cell == progress.last() && progress.size > 1 -> progress = progress.dropLast(1) // undo
            cell in progress -> {}
            step == progress.size + 1 -> {
                val last = progress.last()
                val adjacent = kotlin.math.abs(last.first - cell.first) + kotlin.math.abs(last.second - cell.second) == 1
                if (adjacent) {
                    progress = progress + cell
                    if (progress.size == total) {
                        won = true
                        onWin()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Number Connect • ${difficulty.label}") },
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
                Text("Path: ${progress.size}/$total", style = MaterialTheme.typography.titleMedium)
                if (won) Text("Connected! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::newPuzzle) { Text("New Puzzle") }
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
                    for (r in 0 until n) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until n) {
                                val cell = r to c
                                val step = stepOf[cell] ?: 0
                                val walked = cell in progress
                                val isNextHint = step == progress.size + 1 && step in revealed
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(2.dp)
                                        .background(
                                            when {
                                                walked -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.surface
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            2.dp,
                                            if (isNextHint) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { tap(cell) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        walked -> Text(
                                            step.toString(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (n > 6) 12.sp else 15.sp
                                        )
                                        step in revealed -> Text(
                                            step.toString(),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = if (n > 6) 12.sp else 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { progress = emptyList() }, enabled = progress.isNotEmpty() && !won) {
                    Text("Restart Path")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Start at 1 and tap adjacent squares to walk the hidden path in order. Numbered squares are checkpoints — the path must hit them exactly on that step. Tap your last square to step back.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
