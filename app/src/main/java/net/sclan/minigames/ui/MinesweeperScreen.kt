package net.sclan.minigames.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal const val MS_ROWS = 9
internal const val MS_COLS = 9
internal const val MS_MINES = 10

internal data class MsCell(
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val neighborMines: Int = 0
)

internal enum class MsState { Playing, Won, Lost }

internal fun createMsBoard(avoidRow: Int = -1, avoidCol: Int = -1): List<List<MsCell>> {
    val mines = mutableSetOf<Pair<Int, Int>>()
    while (mines.size < MS_MINES) {
        val r = (0 until MS_ROWS).random()
        val c = (0 until MS_COLS).random()
        if (!(r == avoidRow && c == avoidCol)) mines.add(r to c)
    }
    return List(MS_ROWS) { r ->
        List(MS_COLS) { c ->
            val mine = (r to c) in mines
            val neighbors = if (mine) 0 else (-1..1).sumOf { dr ->
                (-1..1).count { dc ->
                    val nr = r + dr; val nc = c + dc
                    nr in 0 until MS_ROWS && nc in 0 until MS_COLS && (nr to nc) in mines
                }
            }
            MsCell(isMine = mine, neighborMines = neighbors)
        }
    }
}

internal fun revealMs(board: List<List<MsCell>>, startRow: Int, startCol: Int): List<List<MsCell>> {
    val start = board[startRow][startCol]
    if (start.isRevealed || start.isFlagged) return board
    val result = board.map { it.toMutableList() }.toMutableList()
    val queue = ArrayDeque<Pair<Int, Int>>()
    queue.add(startRow to startCol)
    while (queue.isNotEmpty()) {
        val (r, c) = queue.removeFirst()
        if (r !in 0 until MS_ROWS || c !in 0 until MS_COLS) continue
        val cell = result[r][c]
        if (cell.isRevealed || cell.isFlagged) continue
        result[r][c] = cell.copy(isRevealed = true)
        if (cell.neighborMines == 0 && !cell.isMine) {
            for (dr in -1..1) for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                queue.add((r + dr) to (c + dc))
            }
        }
    }
    return result
}

internal fun toggleFlagMs(board: List<List<MsCell>>, row: Int, col: Int): List<List<MsCell>> {
    if (board[row][col].isRevealed) return board
    return board.mapIndexed { r, rowList ->
        rowList.mapIndexed { c, cell ->
            if (r == row && c == col) cell.copy(isFlagged = !cell.isFlagged) else cell
        }
    }
}

internal fun checkWinMs(board: List<List<MsCell>>): Boolean =
    board.all { row -> row.all { it.isRevealed || it.isMine } }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val numberColors = mapOf(
    1 to Color(0xFF1565C0),
    2 to Color(0xFF2E7D32),
    3 to Color(0xFFC62828),
    4 to Color(0xFF4A148C),
    5 to Color(0xFF880E4F),
    6 to Color(0xFF006064),
    7 to Color(0xFF212121),
    8 to Color(0xFF757575)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MinesweeperScreen(
    onBack: () -> Unit,
    onWin: (timeSecs: Long) -> Unit = {}
) {
    var board by remember { mutableStateOf<List<List<MsCell>>?>(null) }
    var gameState by remember { mutableStateOf(MsState.Playing) }
    var flagsLeft by remember { mutableIntStateOf(MS_MINES) }
    var startTimeMs by remember { mutableStateOf(0L) }

    fun reset() {
        board = null
        gameState = MsState.Playing
        flagsLeft = MS_MINES
        startTimeMs = 0L
    }

    fun onTap(row: Int, col: Int) {
        if (gameState != MsState.Playing) return
        val current = board ?: createMsBoard(row, col).also {
            board = it
            startTimeMs = System.currentTimeMillis()
        }
        val cell = current[row][col]
        if (cell.isFlagged || cell.isRevealed) return
        if (cell.isMine) {
            board = current.map { r -> r.map { c -> if (c.isMine) c.copy(isRevealed = true) else c } }
            gameState = MsState.Lost
            return
        }
        val next = revealMs(current, row, col)
        board = next
        if (checkWinMs(next)) {
            gameState = MsState.Won
            val elapsed = if (startTimeMs > 0L)
                (System.currentTimeMillis() - startTimeMs) / 1000L else 0L
            onWin(elapsed)
        }
    }

    fun onLongPress(row: Int, col: Int) {
        if (gameState != MsState.Playing) return
        val current = board ?: return
        if (current[row][col].isRevealed) return
        val adding = !current[row][col].isFlagged
        board = toggleFlagMs(current, row, col)
        flagsLeft += if (adding) -1 else 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minesweeper") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mines: $flagsLeft", style = MaterialTheme.typography.titleMedium)
                when (gameState) {
                    MsState.Won  -> Text("You won!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    MsState.Lost -> Text("Boom!", color = Color.Red, fontWeight = FontWeight.Bold)
                    MsState.Playing -> Text(
                        "Tap · long-press to flag",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(onClick = ::reset) { Text("Reset") }
            }

            Spacer(Modifier.height(12.dp))

            val displayBoard = board ?: List(MS_ROWS) { List(MS_COLS) { MsCell() } }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (r in 0 until MS_ROWS) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (c in 0 until MS_COLS) {
                            val cell = displayBoard[r][c]
                            val bg = when {
                                cell.isRevealed && cell.isMine -> Color(0xFFB71C1C)
                                cell.isRevealed -> Color(0xFFD7D7D7)
                                else -> Color(0xFF9E9E9E)
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(bg)
                                    .combinedClickable(
                                        onClick = { onTap(r, c) },
                                        onLongClick = { onLongPress(r, c) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    !cell.isRevealed && cell.isFlagged ->
                                        Text("🚩", fontSize = 14.sp)
                                    cell.isRevealed && cell.isMine ->
                                        Text("💣", fontSize = 14.sp)
                                    cell.isRevealed && cell.neighborMines > 0 ->
                                        Text(
                                            text = cell.neighborMines.toString(),
                                            color = numberColors[cell.neighborMines] ?: Color.Black,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
