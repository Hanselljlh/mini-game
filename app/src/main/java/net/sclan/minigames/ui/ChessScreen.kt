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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun pieceGlyph(type: PieceType): String = when (type) {
    PieceType.Pawn -> "♟"
    PieceType.Knight -> "♞"
    PieceType.Bishop -> "♝"
    PieceType.Rook -> "♜"
    PieceType.Queen -> "♛"
    PieceType.King -> "♚"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessScreen(
    onBack: () -> Unit,
    onFinished: (playerWon: Boolean) -> Unit = {}
) {
    var state by remember { mutableStateOf(initialChessState()) }
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var thinking by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }

    val status = remember(state) { chessStatus(state) }
    val legal = remember(state, selected) {
        val sel = selected
        if (sel == null) emptyList() else legalMoves(state).filter { it.from == sel }
    }

    fun endReport(playerWon: Boolean) {
        if (!reported) { reported = true; onFinished(playerWon) }
    }

    // Bot (Black) plays when it's its turn.
    LaunchedEffect(state.toMove, status) {
        if (state.toMove == PieceColor.Black &&
            (status == ChessStatus.Ongoing || status == ChessStatus.Check)
        ) {
            thinking = true
            val move = withContext(Dispatchers.Default) { chessBestMove(state, 3) }
            thinking = false
            if (move != null) {
                state = applyChessMove(state, move)
                selected = null
            }
        }
        if (status == ChessStatus.Checkmate) endReport(playerWon = state.toMove == PieceColor.Black)
        else if (status == ChessStatus.Stalemate) endReport(playerWon = false)
    }

    fun reset() {
        state = initialChessState()
        selected = null
        thinking = false
        reported = false
    }

    fun tap(r: Int, c: Int) {
        if (thinking || state.toMove != PieceColor.White) return
        if (status == ChessStatus.Checkmate || status == ChessStatus.Stalemate) return
        val sel = selected
        val piece = state.board[r][c]
        if (sel == null) {
            if (piece != null && piece.color == PieceColor.White) selected = r to c
            return
        }
        val move = legal.firstOrNull { it.to == (r to c) }
        when {
            move != null -> { state = applyChessMove(state, move); selected = null }
            piece != null && piece.color == PieceColor.White -> selected = r to c
            else -> selected = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chess") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
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
                val statusText = when (status) {
                    ChessStatus.Checkmate -> if (state.toMove == PieceColor.Black) "Checkmate — you win! 🏆" else "Checkmate — bot wins."
                    ChessStatus.Stalemate -> "Stalemate — draw."
                    ChessStatus.Check -> if (thinking) "Bot thinking… (check)" else "Check! Your move."
                    ChessStatus.Ongoing -> when {
                        thinking -> "Bot thinking…"
                        state.toMove == PieceColor.White -> "Your move (White)"
                        else -> "Black to move"
                    }
                }
                Text(statusText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(10.dp))

            val legalTargets = legal.map { it.to }.toSet()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(3.dp, Color(0xFF3E2723))
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (r in 0..7) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0..7) {
                                val light = (r + c) % 2 == 0
                                val isSel = selected == (r to c)
                                val bg = when {
                                    isSel -> Color(0xFFBAC53F)
                                    light -> Color(0xFFEEEED2)
                                    else -> Color(0xFF769656)
                                }
                                val piece = state.board[r][c]
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(bg)
                                        .clickable { tap(r, c) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (piece != null) {
                                        Text(
                                            pieceGlyph(piece.type),
                                            fontSize = 28.sp,
                                            color = if (piece.color == PieceColor.White) Color(0xFFFAFAFA) else Color(0xFF1A1A1A)
                                        )
                                    }
                                    if ((r to c) in legalTargets) {
                                        Box(
                                            Modifier
                                                .size(if (piece != null) 40.dp else 16.dp)
                                                .then(
                                                    if (piece != null)
                                                        Modifier.border(3.dp, Color(0x99000000), CircleShape)
                                                    else
                                                        Modifier.background(Color(0x66000000), CircleShape)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "You play White (bottom). Tap a piece to see its legal moves, then tap a square. Pawns auto-promote to a queen. Castling and en passant are supported.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
