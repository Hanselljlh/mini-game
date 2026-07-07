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
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — classic sliding number puzzle (15-puzzle family).
// Board is a list of size n*n; value 0 is the empty hole.
// ---------------------------------------------------------------------------

internal fun solvedSliding(n: Int): List<Int> = (1 until n * n) + listOf(0)

/** Shuffles by applying random legal moves from solved — always solvable. */
internal fun shuffledSliding(n: Int, steps: Int = 300, random: Random = Random.Default): List<Int> {
    var board = solvedSliding(n)
    var previous = -1
    repeat(steps) {
        val moves = slidingMovable(board, n).filter { it != previous }
        val pick = moves[random.nextInt(moves.size)]
        previous = board.indexOf(0)
        board = slidingMove(board, n, pick)!!
    }
    return board
}

/** Indices of tiles orthogonally adjacent to the hole (i.e., movable). */
internal fun slidingMovable(board: List<Int>, n: Int): List<Int> {
    val hole = board.indexOf(0)
    val hr = hole / n
    val hc = hole % n
    return listOf(hr - 1 to hc, hr + 1 to hc, hr to hc - 1, hr to hc + 1)
        .filter { (r, c) -> r in 0 until n && c in 0 until n }
        .map { (r, c) -> r * n + c }
}

/** Slides the tile at [index] into the hole, or null if not adjacent. */
internal fun slidingMove(board: List<Int>, n: Int, index: Int): List<Int>? {
    if (index !in slidingMovable(board, n)) return null
    val hole = board.indexOf(0)
    return board.toMutableList().also {
        it[hole] = it[index]
        it[index] = 0
    }
}

internal fun slidingSolved(board: List<Int>, n: Int): Boolean = board == solvedSliding(n)

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidingPuzzleScreen(
    difficulty: SlidingSize = SlidingSize.Classic,
    onBack: () -> Unit,
    onWin: (moves: Int) -> Unit = {}
) {
    val n = difficulty.n
    var board by remember(difficulty) { mutableStateOf(shuffledSliding(n)) }
    var moves by remember(difficulty) { mutableIntStateOf(0) }
    var won by remember(difficulty) { mutableStateOf(false) }

    fun reset() {
        board = shuffledSliding(n)
        moves = 0
        won = false
    }

    fun tap(index: Int) {
        if (won) return
        val next = slidingMove(board, n, index) ?: return
        board = next
        moves++
        if (slidingSolved(next, n)) {
            won = true
            onWin(moves)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sliding Puzzle • ${difficulty.label}") },
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
                Text("Moves: $moves", style = MaterialTheme.typography.titleMedium)
                if (won) Text("Solved! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("Shuffle") }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(6.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (r in 0 until n) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until n) {
                                val index = r * n + c
                                val value = board[index]
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(3.dp)
                                        .background(
                                            when {
                                                value == 0 -> Color.Transparent
                                                value == index + 1 -> Color(0xFF66BB6A)
                                                else -> MaterialTheme.colorScheme.primaryContainer
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable(enabled = value != 0) { tap(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (value != 0) {
                                        Text(
                                            value.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (n > 4) 18.sp else 24.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tap a tile next to the gap to slide it. Put the numbers in order with the gap last. Green tiles are already home.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
