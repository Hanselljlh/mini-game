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
import androidx.compose.runtime.mutableIntStateOf
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
// Pure game logic (2248-style chain merging)
// Board is rows×cols of powers of two. Chain: adjacent (8-way) cells; each
// next cell must equal the previous value or exactly double it, and the
// first two must be equal. Merged value = smallest power of two ≥ chain sum.
// ---------------------------------------------------------------------------

internal const val MC_ROWS = 6
internal const val MC_COLS = 5

internal fun mcRandomValue(random: Random = Random.Default): Int {
    val exponents = listOf(1, 1, 1, 2, 2, 2, 3, 3, 4, 5) // 2..32, small-biased
    return 1 shl exponents[random.nextInt(exponents.size)]
}

internal fun mcNewBoard(random: Random = Random.Default): List<List<Int>> =
    List(MC_ROWS) { List(MC_COLS) { mcRandomValue(random) } }

internal fun mcAdjacent(a: Pair<Int, Int>, b: Pair<Int, Int>): Boolean {
    val dr = kotlin.math.abs(a.first - b.first)
    val dc = kotlin.math.abs(a.second - b.second)
    return dr <= 1 && dc <= 1 && (dr + dc) > 0
}

/** Can [next] extend a chain whose last cell is [lastCell]? */
internal fun mcCanExtend(board: List<List<Int>>, chain: List<Pair<Int, Int>>, next: Pair<Int, Int>): Boolean {
    if (next in chain) return false
    val last = chain.lastOrNull() ?: return true
    if (!mcAdjacent(last, next)) return false
    val lastV = board[last.first][last.second]
    val nextV = board[next.first][next.second]
    return if (chain.size == 1) nextV == lastV else nextV == lastV || nextV == lastV * 2
}

/** Smallest power of two ≥ sum. */
internal fun mcMergeValue(sum: Int): Int {
    var v = 1
    while (v < sum) v = v shl 1
    return v
}

/** Applies a merge: chain cells vanish, result lands on the last cell, gravity + refill. */
internal fun mcApplyMerge(
    board: List<List<Int>>,
    chain: List<Pair<Int, Int>>,
    random: Random = Random.Default
): Pair<List<List<Int>>, Int> {
    val sum = chain.sumOf { (r, c) -> board[r][c] }
    val result = mcMergeValue(sum)
    val last = chain.last()
    val work = board.map { it.toMutableList() }
    chain.dropLast(1).forEach { (r, c) -> work[r][c] = 0 }
    work[last.first][last.second] = result
    // Gravity per column, refill from top
    for (c in 0 until MC_COLS) {
        val stack = (0 until MC_ROWS).map { work[it][c] }.filter { it > 0 }
        val filled = List(MC_ROWS - stack.size) { mcRandomValue(random) } + stack
        for (r in 0 until MC_ROWS) work[r][c] = filled[r]
    }
    return work.map { it.toList() } to result
}

internal fun mcHasAnyPair(board: List<List<Int>>): Boolean {
    for (r in 0 until MC_ROWS) for (c in 0 until MC_COLS) {
        for (dr in -1..1) for (dc in -1..1) {
            if (dr == 0 && dc == 0) continue
            val nr = r + dr
            val nc = c + dc
            if (nr in 0 until MC_ROWS && nc in 0 until MC_COLS && board[r][c] == board[nr][nc]) return true
        }
    }
    return false
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private fun mcTileColor(value: Int): Color {
    val idx = (31 - Integer.numberOfLeadingZeros(value)).coerceAtLeast(1)
    return sortColors[(idx - 1) % sortColors.size]
}

private fun mcLabel(value: Int): String =
    if (value >= 1024) "${value / 1024}K" else value.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeChainScreen(
    onBack: () -> Unit,
    onBestScore: (score: Int) -> Unit = {}
) {
    var board by remember { mutableStateOf(mcNewBoard()) }
    var chain by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var score by remember { mutableIntStateOf(0) }
    var bestTile by remember { mutableIntStateOf(0) }

    fun reset() {
        board = mcNewBoard()
        chain = emptyList()
        score = 0
        bestTile = 0
    }

    fun tap(cell: Pair<Int, Int>) {
        when {
            chain.isEmpty() -> chain = listOf(cell)
            cell == chain.last() && chain.size >= 2 -> {
                // Confirm merge by re-tapping the last cell
                val (next, result) = mcApplyMerge(board, chain)
                board = if (mcHasAnyPair(next)) next else mcNewBoard()
                score += result
                if (result > bestTile) bestTile = result
                onBestScore(score)
                chain = emptyList()
            }
            cell in chain -> {
                // Tap earlier cell: trim chain back to that cell
                chain = chain.subList(0, chain.indexOf(cell) + 1)
            }
            mcCanExtend(board, chain, cell) -> chain = chain + cell
            else -> chain = listOf(cell)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merge Chain") },
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
                if (bestTile > 0) Text("Best tile: ${mcLabel(bestTile)}", fontWeight = FontWeight.SemiBold)
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(MC_COLS.toFloat() / MC_ROWS)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (r in 0 until MC_ROWS) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until MC_COLS) {
                                val cell = r to c
                                val inChain = cell in chain
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(2.dp)
                                        .background(mcTileColor(board[r][c]), RoundedCornerShape(8.dp))
                                        .border(
                                            3.dp,
                                            when {
                                                cell == chain.lastOrNull() && chain.size >= 2 -> Color.White
                                                inChain -> Color.White.copy(alpha = 0.6f)
                                                else -> Color.Transparent
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { tap(cell) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        mcLabel(board[r][c]),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            if (chain.size >= 2) {
                val sum = chain.sumOf { (r, c) -> board[r][c] }
                Text(
                    "Chain of ${chain.size} → merges into ${mcLabel(mcMergeValue(sum))}. Tap the last tile again to merge.",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    "Tap tiles to build a chain: start with two equal numbers, then keep matching or doubling. Re-tap the last tile to merge.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = { chain = emptyList() }, enabled = chain.isNotEmpty()) { Text("Clear Chain") }
        }
    }
}
