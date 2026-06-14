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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(onBack: () -> Unit) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var current by remember { mutableStateOf("X") }
    var status by remember { mutableStateOf("X's turn") }
    var gameOver by remember { mutableStateOf(false) }

    fun reset() {
        board = List(9) { "" }
        current = "X"
        status = "X's turn"
        gameOver = false
    }

    fun tap(index: Int) {
        if (gameOver || board[index].isNotEmpty()) return
        val next = board.toMutableList().also { it[index] = current }
        board = next
        when (val w = winner(next)) {
            null -> if (next.all { it.isNotEmpty() }) {
                status = "Draw!"; gameOver = true
            } else {
                current = if (current == "X") "O" else "X"
                status = "$current's turn"
            }
            else -> { status = "$w wins!"; gameOver = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tic Tac Toe") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(status, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            for (row in 0..2) {
                Row {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .border(2.dp, MaterialTheme.colorScheme.outline)
                                .clickable { tap(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = board[idx],
                                fontSize = 40.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = ::reset) { Text("New Game") }
        }
    }
}
