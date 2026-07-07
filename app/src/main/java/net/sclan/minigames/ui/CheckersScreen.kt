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
import androidx.compose.foundation.shape.CircleShape
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
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — checkers/draughts on 8×8.
// Board values: 0 empty, 1 player man, 2 bot man, 3 player king, 4 bot king.
// Player moves up (decreasing row); bot moves down. Captures are mandatory.
// ---------------------------------------------------------------------------

internal const val CK_SIZE = 8

internal typealias CkBoard = List<List<Int>>

internal fun ckIsPlayer(v: Int): Boolean = v == 1 || v == 3
internal fun ckIsBot(v: Int): Boolean = v == 2 || v == 4
internal fun ckIsKing(v: Int): Boolean = v == 3 || v == 4

internal fun newCheckersBoard(): CkBoard = List(CK_SIZE) { r ->
    List(CK_SIZE) { c ->
        when {
            (r + c) % 2 == 0 -> 0
            r < 3 -> 2
            r > 4 -> 1
            else -> 0
        }
    }
}

internal data class CkMove(
    val from: Pair<Int, Int>,
    val to: Pair<Int, Int>,
    val captured: Pair<Int, Int>? = null
)

private fun ckDirections(v: Int): List<Pair<Int, Int>> = when {
    ckIsKing(v) -> listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
    v == 1 -> listOf(-1 to -1, -1 to 1)
    else -> listOf(1 to -1, 1 to 1)
}

internal fun ckMovesFor(board: CkBoard, cell: Pair<Int, Int>): List<CkMove> {
    val (r, c) = cell
    val v = board[r][c]
    if (v == 0) return emptyList()
    val mine = if (ckIsPlayer(v)) ::ckIsPlayer else ::ckIsBot
    val moves = mutableListOf<CkMove>()
    for ((dr, dc) in ckDirections(v)) {
        val nr = r + dr
        val nc = c + dc
        if (nr !in 0 until CK_SIZE || nc !in 0 until CK_SIZE) continue
        when {
            board[nr][nc] == 0 -> moves.add(CkMove(cell, nr to nc))
            !mine(board[nr][nc]) -> {
                val jr = nr + dr
                val jc = nc + dc
                if (jr in 0 until CK_SIZE && jc in 0 until CK_SIZE && board[jr][jc] == 0) {
                    moves.add(CkMove(cell, jr to jc, nr to nc))
                }
            }
        }
    }
    return moves
}

/** All legal moves for a side; if any capture exists, only captures are legal. */
internal fun ckAllMoves(board: CkBoard, forPlayer: Boolean): List<CkMove> {
    val side = if (forPlayer) ::ckIsPlayer else ::ckIsBot
    val all = mutableListOf<CkMove>()
    for (r in 0 until CK_SIZE) for (c in 0 until CK_SIZE) {
        if (side(board[r][c])) all += ckMovesFor(board, r to c)
    }
    val captures = all.filter { it.captured != null }
    return captures.ifEmpty { all }
}

internal fun ckApply(board: CkBoard, move: CkMove): CkBoard {
    val work = board.map { it.toMutableList() }
    var v = work[move.from.first][move.from.second]
    work[move.from.first][move.from.second] = 0
    move.captured?.let { (cr, cc) -> work[cr][cc] = 0 }
    // Promotion
    if (v == 1 && move.to.first == 0) v = 3
    if (v == 2 && move.to.first == CK_SIZE - 1) v = 4
    work[move.to.first][move.to.second] = v
    return work.map { it.toList() }
}

/** After a capture, the same piece must continue jumping if it can. */
internal fun ckChainCaptures(board: CkBoard, at: Pair<Int, Int>): List<CkMove> =
    ckMovesFor(board, at).filter { it.captured != null }

internal fun ckBotMove(board: CkBoard, random: Random = Random.Default): CkMove? {
    val moves = ckAllMoves(board, forPlayer = false)
    if (moves.isEmpty()) return null
    return moves[random.nextInt(moves.size)]
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckersScreen(
    onBack: () -> Unit,
    onFinished: (playerWon: Boolean) -> Unit = {}
) {
    var board by remember { mutableStateOf(newCheckersBoard()) }
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var playerTurn by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("Your move — you're red.") }
    var over by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }

    fun reset() {
        board = newCheckersBoard()
        selected = null
        playerTurn = true
        status = "Your move — you're red."
        over = false
        reported = false
    }

    fun endIfNoMoves() {
        if (over) return
        val side = playerTurn
        if (ckAllMoves(board, side).isEmpty()) {
            over = true
            status = if (side) "No moves left — bot wins." else "Bot is stuck — you win! 🎉"
            if (!reported) {
                reported = true
                onFinished(!side)
            }
        }
    }

    fun botTurn() {
        playerTurn = false
        var move = ckBotMove(board)
        while (move != null) {
            val wasCapture = move.captured != null
            board = ckApply(board, move)
            if (wasCapture) {
                val chain = ckChainCaptures(board, move.to)
                move = chain.randomOrNull()
            } else move = null
        }
        playerTurn = true
        status = "Your move."
        endIfNoMoves()
    }

    fun tap(cell: Pair<Int, Int>) {
        if (over || !playerTurn) return
        val (r, c) = cell
        val legal = ckAllMoves(board, forPlayer = true)
        if (ckIsPlayer(board[r][c])) {
            selected = cell
            return
        }
        val sel = selected ?: return
        val move = legal.firstOrNull { it.from == sel && it.to == cell } ?: return
        val wasCapture = move.captured != null
        board = ckApply(board, move)
        selected = null
        if (wasCapture) {
            val chain = ckChainCaptures(board, move.to)
            if (chain.isNotEmpty()) {
                selected = move.to
                status = "Keep jumping!"
                return
            }
        }
        status = "Bot thinking…"
        botTurn()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkers") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
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
                Text(status, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(12.dp))

            val legalTargets = selected?.let { sel ->
                ckAllMoves(board, forPlayer = true).filter { it.from == sel }.map { it.to }
            } ?: emptyList()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(3.dp, Color(0xFF4E342E))
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (r in 0 until CK_SIZE) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until CK_SIZE) {
                                val dark = (r + c) % 2 == 1
                                val cell = r to c
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            when {
                                                cell in legalTargets -> Color(0xFF81C784)
                                                dark -> Color(0xFF6D4C41)
                                                else -> Color(0xFFD7CCC8)
                                            }
                                        )
                                        .clickable { tap(cell) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val v = board[r][c]
                                    if (v != 0) {
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .padding(5.dp)
                                                .background(
                                                    if (ckIsPlayer(v)) Color(0xFFD32F2F) else Color(0xFF212121),
                                                    CircleShape
                                                )
                                                .border(
                                                    3.dp,
                                                    if (selected == cell) Color(0xFFFFD54F) else Color.Transparent,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (ckIsKing(v)) Text("♛", color = Color(0xFFFFD54F))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Tap a red piece, then a highlighted square. Jumps are mandatory and chain automatically. Reach the far row to crown a king.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
