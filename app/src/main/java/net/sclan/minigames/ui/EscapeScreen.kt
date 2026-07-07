package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic (Rush-Hour-style block escape on a 6×6 grid)
// The red block (id 0) is horizontal on row 2 and must reach the right edge.
// Puzzles are generated procedurally and never repeat: random layouts are
// proven solvable by BFS, and the BFS solution length ("par") is used to
// band them into difficulties.
// ---------------------------------------------------------------------------

internal const val ESC_SIZE = 6
internal const val ESC_EXIT_ROW = 2

internal data class EscBlock(val id: Int, val row: Int, val col: Int, val len: Int, val horizontal: Boolean) {
    fun cells(): List<Pair<Int, Int>> =
        (0 until len).map { i -> if (horizontal) row to (col + i) else (row + i) to col }
}

internal fun escOccupied(blocks: List<EscBlock>, except: Int = -1): Set<Pair<Int, Int>> =
    blocks.filter { it.id != except }.flatMap { it.cells() }.toSet()

/** Moves a block one cell along its axis (delta = ±1). Returns null if blocked. */
internal fun escMove(blocks: List<EscBlock>, id: Int, delta: Int): List<EscBlock>? {
    val block = blocks.first { it.id == id }
    val moved = if (block.horizontal) block.copy(col = block.col + delta) else block.copy(row = block.row + delta)
    if (moved.cells().any { (r, c) -> r !in 0 until ESC_SIZE || c !in 0 until ESC_SIZE }) return null
    val others = escOccupied(blocks, except = id)
    if (moved.cells().any { it in others }) return null
    return blocks.map { if (it.id == id) moved else it }
}

internal fun escSolved(blocks: List<EscBlock>): Boolean {
    val red = blocks.first { it.id == 0 }
    return red.col + red.len >= ESC_SIZE
}

/**
 * Minimum number of single-cell slides to solve, or null if unsolvable.
 * Breadth-first over block positions; [maxStates] bounds the search so it can
 * never run away (a lesson learned the hard way with Number Connect).
 */
internal fun escMinMoves(start: List<EscBlock>, maxStates: Int = 120_000): Int? {
    fun key(b: List<EscBlock>) = b.joinToString("|") { "${it.row},${it.col}" }
    val seen = hashSetOf(key(start))
    var frontier = listOf(start)
    var depth = 0
    while (frontier.isNotEmpty() && seen.size < maxStates) {
        if (frontier.any { escSolved(it) }) return depth
        val next = mutableListOf<List<EscBlock>>()
        for (state in frontier) {
            for (block in state) {
                for (delta in intArrayOf(-1, 1)) {
                    escMove(state, block.id, delta)?.let { n ->
                        if (seen.add(key(n))) next.add(n)
                    }
                }
            }
        }
        frontier = next
        depth++
    }
    return null
}

/** Always-solvable emergency layout (par ≈ 5) if generation somehow strikes out. */
internal val ESC_FALLBACK: List<EscBlock> = listOf(
    EscBlock(0, 2, 0, 2, true),
    EscBlock(1, 2, 3, 2, false),
    EscBlock(2, 4, 0, 3, true)
)

private fun distToBand(par: Int, d: EscapeDifficulty): Int = when {
    par < d.minMoves -> d.minMoves - par
    par > d.maxMoves -> par - d.maxMoves
    else -> 0
}

/**
 * Generates a fresh random puzzle whose BFS par falls inside the difficulty
 * band (or the closest solvable layout found if the band can't be hit within
 * [attempts]). Returns the puzzle and its par. Every call is a new puzzle —
 * there is no fixed level list to exhaust.
 */
internal fun generateEscapePuzzle(
    difficulty: EscapeDifficulty,
    random: Random = Random.Default,
    attempts: Int = 250
): Pair<List<EscBlock>, Int> {
    var best: Pair<List<EscBlock>, Int>? = null
    repeat(attempts) {
        val blocks = mutableListOf(EscBlock(0, ESC_EXIT_ROW, random.nextInt(2), 2, true))
        val target = difficulty.blockers.random(random)
        var id = 1
        var placeTries = 0
        while (id <= target && placeTries < 90) {
            placeTries++
            val horizontal = random.nextFloat() < 0.4f
            val len = if (random.nextFloat() < 0.3f) 3 else 2
            val row: Int
            val col: Int
            if (horizontal) {
                // Horizontal blockers stay off the exit row — only vertical
                // blocks may cross the red block's lane.
                var r = random.nextInt(ESC_SIZE)
                if (r == ESC_EXIT_ROW) r = if (random.nextBoolean()) ESC_EXIT_ROW - 1 else ESC_EXIT_ROW + 1
                row = r
                col = random.nextInt(ESC_SIZE - len + 1)
            } else {
                row = random.nextInt(ESC_SIZE - len + 1)
                col = random.nextInt(ESC_SIZE)
            }
            val candidate = EscBlock(id, row, col, len, horizontal)
            if (candidate.cells().none { it in escOccupied(blocks) }) {
                blocks.add(candidate)
                id++
            }
        }
        val par = escMinMoves(blocks) ?: return@repeat
        if (par < 1) return@repeat // red lane already clear — boring
        val puzzle = blocks.toList() to par
        if (par in difficulty.minMoves..difficulty.maxMoves) return puzzle
        if (best == null || distToBand(par, difficulty) < distToBand(best!!.second, difficulty)) best = puzzle
    }
    return best ?: (ESC_FALLBACK to (escMinMoves(ESC_FALLBACK) ?: 5))
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val escBlockColors = listOf(
    Color(0xFFE53935), // red hero
    Color(0xFF8D6E63), Color(0xFF5C6BC0), Color(0xFF26A69A),
    Color(0xFFFFB300), Color(0xFF7E57C2), Color(0xFF66BB6A), Color(0xFFEC407A)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscapeScreen(
    difficulty: EscapeDifficulty = EscapeDifficulty.Casual,
    onBack: () -> Unit,
    onPuzzleSolved: (moves: Int) -> Unit = {}
) {
    var puzzleNumber by remember(difficulty) { mutableIntStateOf(1) }
    var puzzle by remember(difficulty) { mutableStateOf(generateEscapePuzzle(difficulty)) }
    var blocks by remember(difficulty, puzzleNumber) { mutableStateOf(puzzle.first) }
    var selected by remember(difficulty, puzzleNumber) { mutableIntStateOf(0) }
    var moves by remember(difficulty, puzzleNumber) { mutableIntStateOf(0) }
    var won by remember(difficulty, puzzleNumber) { mutableStateOf(false) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }

    fun resetSame() {
        blocks = puzzle.first
        selected = 0
        moves = 0
        won = false
    }

    fun nextPuzzle() {
        puzzle = generateEscapePuzzle(difficulty)
        puzzleNumber++
    }

    fun trySlide(dx: Int, dy: Int) {
        if (won) return
        val block = blocks.first { it.id == selected }
        val delta = if (block.horizontal) dx else dy
        if (delta == 0) return
        val next = escMove(blocks, selected, delta) ?: return
        blocks = next
        moves++
        if (escSolved(next)) {
            won = true
            onPuzzleSolved(moves)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escape • ${difficulty.label} #$puzzleNumber") },
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
                Text("Moves: $moves • Par: ${puzzle.second}", style = MaterialTheme.typography.titleMedium)
                if (won) Text("Escaped! 🚗", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::resetSame) { Text("Reset") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .onSizeChanged { boardSize = it }
                    .pointerInput(difficulty, puzzleNumber, selected, won) {
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
                                val threshold = 25f
                                when {
                                    abs(dx) > abs(dy) && abs(dx) > threshold -> trySlide(if (dx > 0) 1 else -1, 0)
                                    abs(dy) > threshold -> trySlide(0, if (dy > 0) 1 else -1)
                                }
                            }
                        )
                    }
            ) {
                blocks.forEach { block ->
                    val w = if (block.horizontal) block.len else 1
                    val h = if (block.horizontal) 1 else block.len
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    boardSize.width * block.col / ESC_SIZE,
                                    boardSize.height * block.row / ESC_SIZE
                                )
                            }
                            .fillMaxWidth(w.toFloat() / ESC_SIZE)
                            .aspectRatio(w.toFloat() / h)
                            .padding(3.dp)
                            .background(
                                escBlockColors[block.id % escBlockColors.size],
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                3.dp,
                                if (selected == block.id) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selected = block.id }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (won) {
                Button(onClick = ::nextPuzzle) { Text("Next Puzzle") }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                "Tap a block to select it, then swipe to slide it along its track. Get the red block to the right edge. " +
                    "Every puzzle is freshly generated — they never repeat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
