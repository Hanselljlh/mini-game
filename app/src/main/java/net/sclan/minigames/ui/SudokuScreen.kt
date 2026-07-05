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
import androidx.compose.foundation.layout.size
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
// Pure game logic
// ---------------------------------------------------------------------------

internal data class SudokuPuzzle(
    val size: Int,
    val boxRows: Int,
    val boxCols: Int,
    val solution: List<List<Int>>,
    val givens: List<List<Int>> // 0 = blank
)

/** Backtracking fill with randomized candidate order — fast for 4/6/9. */
internal fun generateSolvedSudoku(size: Int, boxRows: Int, boxCols: Int, random: Random = Random.Default): List<List<Int>> {
    val grid = Array(size) { IntArray(size) }

    fun canPlace(r: Int, c: Int, v: Int): Boolean {
        for (i in 0 until size) {
            if (grid[r][i] == v || grid[i][c] == v) return false
        }
        val br = (r / boxRows) * boxRows
        val bc = (c / boxCols) * boxCols
        for (rr in br until br + boxRows) for (cc in bc until bc + boxCols) {
            if (grid[rr][cc] == v) return false
        }
        return true
    }

    fun fill(pos: Int): Boolean {
        if (pos == size * size) return true
        val r = pos / size
        val c = pos % size
        val candidates = (1..size).shuffled(random)
        for (v in candidates) {
            if (canPlace(r, c, v)) {
                grid[r][c] = v
                if (fill(pos + 1)) return true
                grid[r][c] = 0
            }
        }
        return false
    }

    fill(0)
    return grid.map { it.toList() }
}

internal fun generateSudoku(difficulty: SudokuDifficulty, random: Random = Random.Default): SudokuPuzzle {
    val solution = generateSolvedSudoku(difficulty.size, difficulty.boxRows, difficulty.boxCols, random)
    val size = difficulty.size
    val allCells = (0 until size * size).shuffled(random)
    val keep = allCells.take(difficulty.givens).toSet()
    val givens = List(size) { r ->
        List(size) { c -> if (r * size + c in keep) solution[r][c] else 0 }
    }
    return SudokuPuzzle(size, difficulty.boxRows, difficulty.boxCols, solution, givens)
}

/** Positions that conflict with another equal value in the same row/col/box. */
internal fun sudokuConflicts(board: List<List<Int>>, boxRows: Int, boxCols: Int): Set<Pair<Int, Int>> {
    val size = board.size
    val conflicts = mutableSetOf<Pair<Int, Int>>()
    for (r in 0 until size) for (c in 0 until size) {
        val v = board[r][c]
        if (v == 0) continue
        for (i in 0 until size) {
            if (i != c && board[r][i] == v) conflicts.add(r to c)
            if (i != r && board[i][c] == v) conflicts.add(r to c)
        }
        val br = (r / boxRows) * boxRows
        val bc = (c / boxCols) * boxCols
        for (rr in br until br + boxRows) for (cc in bc until bc + boxCols) {
            if ((rr != r || cc != c) && board[rr][cc] == v) conflicts.add(r to c)
        }
    }
    return conflicts
}

internal fun sudokuSolved(board: List<List<Int>>, boxRows: Int, boxCols: Int): Boolean =
    board.all { row -> row.all { it != 0 } } && sudokuConflicts(board, boxRows, boxCols).isEmpty()

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    difficulty: SudokuDifficulty = SudokuDifficulty.Mini,
    onBack: () -> Unit,
    onWin: () -> Unit = {}
) {
    var puzzle by remember(difficulty) { mutableStateOf(generateSudoku(difficulty)) }
    var board by remember(difficulty) { mutableStateOf(puzzle.givens) }
    var selected by remember(difficulty) { mutableStateOf<Pair<Int, Int>?>(null) }
    var won by remember(difficulty) { mutableStateOf(false) }

    fun reset() {
        puzzle = generateSudoku(difficulty)
        board = puzzle.givens
        selected = null
        won = false
    }

    fun setCell(value: Int) {
        val sel = selected ?: return
        if (won) return
        val (r, c) = sel
        if (puzzle.givens[r][c] != 0) return // givens are locked
        board = board.mapIndexed { rr, row ->
            row.mapIndexed { cc, v -> if (rr == r && cc == c) value else v }
        }
        if (sudokuSolved(board, puzzle.boxRows, puzzle.boxCols)) {
            won = true
            onWin()
        }
    }

    val conflicts = sudokuConflicts(board, puzzle.boxRows, puzzle.boxCols)
    val size = puzzle.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mini Sudoku • ${difficulty.label}") },
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
                if (won) Text("Solved! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                else Text("Fill every cell", style = MaterialTheme.typography.titleMedium)
                Button(onClick = ::reset) { Text("New Puzzle") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (r in 0 until size) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until size) {
                                val cell = r to c
                                val isGiven = puzzle.givens[r][c] != 0
                                val value = board[r][c]
                                val boxShade = ((r / puzzle.boxRows) + (c / puzzle.boxCols)) % 2 == 0
                                val bg = when {
                                    cell == selected -> MaterialTheme.colorScheme.tertiaryContainer
                                    boxShade -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .background(bg)
                                        .clickable { selected = cell },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (value != 0) {
                                        Text(
                                            value.toString(),
                                            fontSize = if (size > 6) 16.sp else 20.sp,
                                            fontWeight = if (isGiven) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                cell in conflicts -> Color.Red
                                                isGiven -> MaterialTheme.colorScheme.onSurface
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Number pad
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..size).chunked(if (size > 6) 5 else size).forEach { rowNums ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowNums.forEach { num ->
                            OutlinedButton(
                                onClick = { setCell(num) },
                                modifier = Modifier.size(48.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text(num.toString()) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { setCell(0) }) { Text("Erase") }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Tap a cell, then a number. Bold numbers are fixed. Red means conflict.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
