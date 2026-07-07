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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Pure game logic (0 = empty, 1 = red/player, 2 = yellow/bot or player 2)
// ---------------------------------------------------------------------------

internal const val C4_COLS = 7
internal const val C4_ROWS = 6

internal typealias C4Board = List<List<Int>>

internal fun emptyC4Board(): C4Board = List(C4_ROWS) { List(C4_COLS) { 0 } }

/** Returns the board after dropping into [col], or null if the column is full. */
internal fun dropC4(board: C4Board, col: Int, player: Int): C4Board? {
    val row = (C4_ROWS - 1 downTo 0).firstOrNull { board[it][col] == 0 } ?: return null
    return board.mapIndexed { r, cells ->
        cells.mapIndexed { c, v -> if (r == row && c == col) player else v }
    }
}

internal fun winnerC4(board: C4Board): Int {
    val dirs = listOf(1 to 0, 0 to 1, 1 to 1, 1 to -1)
    for (r in 0 until C4_ROWS) for (c in 0 until C4_COLS) {
        val p = board[r][c]
        if (p == 0) continue
        for ((dc, dr) in dirs) {
            var count = 1
            var rr = r + dr
            var cc = c + dc
            while (rr in 0 until C4_ROWS && cc in 0 until C4_COLS && board[rr][cc] == p) {
                count++
                if (count == 4) return p
                rr += dr
                cc += dc
            }
        }
    }
    return 0
}

internal fun isFullC4(board: C4Board): Boolean = board[0].all { it != 0 }

internal fun validColsC4(board: C4Board): List<Int> =
    (0 until C4_COLS).filter { board[0][it] == 0 }

internal fun easyBotC4(board: C4Board): Int? {
    val valid = validColsC4(board)
    return valid.minByOrNull { kotlin.math.abs(it - 3) } // simple: play toward center
}

internal fun smartBotC4(board: C4Board, bot: Int = 2, human: Int = 1): Int? {
    val valid = validColsC4(board)
    // 1. Win now
    valid.firstOrNull { col -> dropC4(board, col, bot)?.let { winnerC4(it) == bot } == true }
        ?.let { return it }
    // 2. Block opponent's win
    valid.firstOrNull { col -> dropC4(board, col, human)?.let { winnerC4(it) == human } == true }
        ?.let { return it }
    // 3. Avoid handing the opponent a win directly above our move
    val safe = valid.filter { col ->
        val after = dropC4(board, col, bot) ?: return@filter false
        validColsC4(after).none { c2 -> dropC4(after, c2, human)?.let { winnerC4(it) == human } == true }
    }
    val pool = safe.ifEmpty { valid }
    return pool.minByOrNull { kotlin.math.abs(it - 3) }
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourInARowScreen(
    mode: FourInARowMode = FourInARowMode.TwoPlayer,
    onBack: () -> Unit,
    onFinished: (playerWon: Boolean) -> Unit = {}
) {
    var board by remember(mode) { mutableStateOf(emptyC4Board()) }
    var current by remember(mode) { mutableStateOf(1) }
    var status by remember(mode) { mutableStateOf(if (mode == FourInARowMode.TwoPlayer) "Red's turn" else "Your turn") }
    var gameOver by remember(mode) { mutableStateOf(false) }

    fun reset() {
        board = emptyC4Board()
        current = 1
        status = if (mode == FourInARowMode.TwoPlayer) "Red's turn" else "Your turn"
        gameOver = false
    }

    fun nameOf(p: Int): String = when {
        mode != FourInARowMode.TwoPlayer -> if (p == 1) "You" else "Bot"
        p == 1 -> "Red"
        else -> "Yellow"
    }

    fun checkEnd(after: C4Board): Boolean {
        val w = winnerC4(after)
        if (w != 0) {
            status = if (mode != FourInARowMode.TwoPlayer && w == 1) "You win!" else "${nameOf(w)} win${if (nameOf(w) == "You") "" else "s"}!"
            gameOver = true
            onFinished(w == 1)
            return true
        }
        if (isFullC4(after)) {
            status = "Draw!"
            gameOver = true
            onFinished(false)
            return true
        }
        return false
    }

    fun botTurn(after: C4Board) {
        val col = when (mode) {
            FourInARowMode.EasyBot -> easyBotC4(after)
            FourInARowMode.SmartBot -> smartBotC4(after)
            FourInARowMode.TwoPlayer -> null
        } ?: return
        val next = dropC4(after, col, 2) ?: return
        board = next
        if (!checkEnd(next)) status = "Your turn"
    }

    fun tapColumn(col: Int) {
        if (gameOver) return
        if (mode != FourInARowMode.TwoPlayer && current != 1) return
        val next = dropC4(board, col, current) ?: return
        board = next
        if (checkEnd(next)) return
        if (mode == FourInARowMode.TwoPlayer) {
            current = if (current == 1) 2 else 1
            status = "${nameOf(current)}'s turn"
        } else {
            botTurn(next)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Four in a Row • ${mode.label}") },
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
                Text(status, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(C4_COLS.toFloat() / C4_ROWS)
                    .background(Color(0xFF1565C0), RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    for (c in 0 until C4_COLS) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { tapColumn(c) }
                        ) {
                            for (r in 0 until C4_ROWS) {
                                val color = when (board[r][c]) {
                                    1 -> Color(0xFFE53935)
                                    2 -> Color(0xFFFDD835)
                                    else -> Color(0xFF0D47A1)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(3.dp)
                                        .background(color, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tap a column to drop your disc. Connect four across, down, or diagonally.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
