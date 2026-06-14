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

internal const val MS_ROWS = 9
internal const val MS_COLS = 9
internal const val MS_MINES = 10
internal val DEFAULT_MS_CONFIG = MinesweeperConfig(MS_ROWS, MS_COLS, MS_MINES)

internal data class MsCell(
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val neighborMines: Int = 0
)

internal enum class MsState { Playing, Won, Lost }

internal fun createMsBoard(
    avoidRow: Int = -1,
    avoidCol: Int = -1,
    config: MinesweeperConfig = DEFAULT_MS_CONFIG
): List<List<MsCell>> {
    val mines = mutableSetOf<Pair<Int, Int>>()
    while (mines.size < config.mines) {
        val r = (0 until config.rows).random()
        val c = (0 until config.cols).random()
        if (!(r == avoidRow && c == avoidCol)) mines.add(r to c)
    }
    return List(config.rows) { r ->
        List(config.cols) { c ->
            val mine = (r to c) in mines
            val neighbors = if (mine) 0 else (-1..1).sumOf { dr ->
                (-1..1).count { dc ->
                    val nr = r + dr; val nc = c + dc
                    nr in 0 until config.rows && nc in 0 until config.cols && (nr to nc) in mines
                }
            }
            MsCell(isMine = mine, neighborMines = neighbors)
        }
    }
}

internal fun revealMs(
    board: List<List<MsCell>>,
    startRow: Int,
    startCol: Int,
    config: MinesweeperConfig = MinesweeperConfig(board.size, board.firstOrNull()?.size ?: 0, 0)
): List<List<MsCell>> {
    val start = board[startRow][startCol]
    if (start.isRevealed || start.isFlagged) return board
    val result = board.map { it.toMutableList() }.toMutableList()
    val queue = ArrayDeque<Pair<Int, Int>>()
    queue.add(startRow to startCol)
    while (queue.isNotEmpty()) {
        val (r, c) = queue.removeFirst()
        if (r !in 0 until config.rows || c !in 0 until config.cols) continue
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
        rowList.mapIndexed { c, cell -> if (r == row && c == col) cell.copy(isFlagged = !cell.isFlagged) else cell }
    }
}

internal fun checkWinMs(board: List<List<MsCell>>): Boolean =
    board.all { row -> row.all { it.isRevealed || it.isMine } }

private val numberColors = mapOf(
    1 to Color(0xFF1565C0), 2 to Color(0xFF2E7D32), 3 to Color(0xFFC62828),
    4 to Color(0xFF4A148C), 5 to Color(0xFF880E4F), 6 to Color(0xFF006064),
    7 to Color(0xFF212121), 8 to Color(0xFF757575)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MinesweeperScreen(
    difficulty: MinesweeperDifficulty = MinesweeperDifficulty.Normal,
    onBack: () -> Unit,
    onWin: (timeSecs: Long) -> Unit = {}
) {
    val config = difficulty.config
    var board by remember(difficulty) { mutableStateOf<List<List<MsCell>>?>(null) }
    var gameState by remember(difficulty) { mutableStateOf(MsState.Playing) }
    var flagsLeft by remember(difficulty) { mutableIntStateOf(config.mines) }
    var startTimeMs by remember(difficulty) { mutableStateOf(0L) }

    fun reset() {
        board = null
        gameState = MsState.Playing
        flagsLeft = config.mines
        startTimeMs = 0L
    }

    fun onTap(row: Int, col: Int) {
        if (gameState != MsState.Playing) return
        val current = board ?: createMsBoard(row, col, config).also {
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
        val next = revealMs(current, row, col, config)
        board = next
        if (checkWinMs(next)) {
            gameState = MsState.Won
            val elapsed = if (startTimeMs > 0L) (System.currentTimeMillis() - startTimeMs) / 1000L else 0L
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
                title = { Text("Minesweeper • ${difficulty.label}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mines: $flagsLeft", style = MaterialTheme.typography.titleMedium)
                when (gameState) {
                    MsState.Won -> Text("You won!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    MsState.Lost -> Text("Boom!", color = Color.Red, fontWeight = FontWeight.Bold)
                    MsState.Playing -> Text("Tap · hold flag", style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = ::reset) { Text("Reset") }
            }

            Spacer(Modifier.height(12.dp))
            val displayBoard = board ?: List(config.rows) { List(config.cols) { MsCell() } }
            val cellSize = when (difficulty) {
                MinesweeperDifficulty.Easy -> 36.dp
                MinesweeperDifficulty.Normal -> 34.dp
                MinesweeperDifficulty.Hard -> 26.dp
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (r in 0 until config.rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (c in 0 until config.cols) {
                            val cell = displayBoard[r][c]
                            val bg = when {
                                cell.isRevealed && cell.isMine -> Color(0xFFB71C1C)
                                cell.isRevealed -> Color(0xFFE0E0E0)
                                else -> Color(0xFF78909C)
                            }
                            Box(
                                modifier = Modifier.size(cellSize).background(bg, RoundedCornerShape(4.dp)).combinedClickable(
                                    onClick = { onTap(r, c) },
                                    onLongClick = { onLongPress(r, c) }
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    !cell.isRevealed && cell.isFlagged -> Text("⚑", fontSize = 14.sp, color = Color.White)
                                    cell.isRevealed && cell.isMine -> Text("✹", fontSize = 14.sp, color = Color.White)
                                    cell.isRevealed && cell.neighborMines > 0 -> Text(
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
