package net.sclan.minigames.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val WIN_LINES = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
    listOf(0, 4, 8), listOf(2, 4, 6)
)

internal fun winner(board: List<String>): String? =
    WIN_LINES.firstNotNullOfOrNull { (a, b, c) ->
        board[a].takeIf { it.isNotEmpty() && it == board[b] && it == board[c] }
    }

internal fun chooseEasyBotMove(board: List<String>): Int? = board.indexOfFirst { it.isEmpty() }.takeIf { it >= 0 }

internal fun chooseSmartBotMove(board: List<String>, bot: String = "O", human: String = "X"): Int? {
    fun winningMove(mark: String): Int? = WIN_LINES.firstNotNullOfOrNull { line ->
        val marks = line.map { board[it] }
        if (marks.count { it == mark } == 2 && marks.count { it.isEmpty() } == 1) line.first { board[it].isEmpty() } else null
    }
    return winningMove(bot)
        ?: winningMove(human)
        ?: 4.takeIf { board[it].isEmpty() }
        ?: listOf(0, 2, 6, 8).firstOrNull { board[it].isEmpty() }
        ?: chooseEasyBotMove(board)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(
    difficulty: TicTacToeDifficulty = TicTacToeDifficulty.TwoPlayer,
    onBack: () -> Unit
) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var current by remember { mutableStateOf("X") }
    var status by remember { mutableStateOf(if (difficulty == TicTacToeDifficulty.TwoPlayer) "X's turn" else "Your turn") }
    var gameOver by remember { mutableStateOf(false) }

    fun reset() {
        board = List(9) { "" }
        current = "X"
        status = if (difficulty == TicTacToeDifficulty.TwoPlayer) "X's turn" else "Your turn"
        gameOver = false
    }

    fun finishTurn(next: List<String>): Boolean {
        return when (val w = winner(next)) {
            null -> if (next.all { it.isNotEmpty() }) {
                status = "Draw!"; gameOver = true; true
            } else false
            else -> { status = if (w == "X" && difficulty != TicTacToeDifficulty.TwoPlayer) "You win!" else "$w wins!"; gameOver = true; true }
        }
    }

    fun botMove(fromBoard: List<String>) {
        val move = when (difficulty) {
            TicTacToeDifficulty.EasyBot -> chooseEasyBotMove(fromBoard)
            TicTacToeDifficulty.SmartBot -> chooseSmartBotMove(fromBoard)
            TicTacToeDifficulty.TwoPlayer -> null
        } ?: return
        val next = fromBoard.toMutableList().also { it[move] = "O" }
        board = next
        if (!finishTurn(next)) status = "Your turn"
    }

    fun tap(index: Int) {
        if (gameOver || board[index].isNotEmpty()) return
        if (difficulty != TicTacToeDifficulty.TwoPlayer && current != "X") return
        val next = board.toMutableList().also { it[index] = current }
        board = next
        if (finishTurn(next)) return
        if (difficulty == TicTacToeDifficulty.TwoPlayer) {
            current = if (current == "X") "O" else "X"
            status = "$current's turn"
        } else {
            status = "Bot thinking..."
            botMove(next)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tic Tac Toe • ${difficulty.label}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(status, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(24.dp))
            for (row in 0..2) {
                Row {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        Box(
                            modifier = Modifier.size(96.dp).border(2.dp, MaterialTheme.colorScheme.outline).clickable { tap(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = board[idx], fontSize = 40.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = ::reset) { Text("New Game") }
        }
    }
}
