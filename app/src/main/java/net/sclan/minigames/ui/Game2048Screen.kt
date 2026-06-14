package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal typealias Board2048 = List<List<Int>>

internal enum class Dir2048 { Left, Right, Up, Down }

internal fun emptyBoard2048(): Board2048 = List(4) { List(4) { 0 } }

internal fun addRandomTile(board: Board2048): Board2048 {
    val empties = (0..3).flatMap { r -> (0..3).mapNotNull { c -> if (board[r][c] == 0) r to c else null } }
    if (empties.isEmpty()) return board
    val (r, c) = empties[Random.nextInt(empties.size)]
    val value = if (Random.nextFloat() < 0.9f) 2 else 4
    return board.mapIndexed { ri, row ->
        row.mapIndexed { ci, v -> if (ri == r && ci == c) value else v }
    }
}

internal fun startingBoard2048(difficulty: TileMergeDifficulty = TileMergeDifficulty.Normal): Board2048 {
    var board = emptyBoard2048()
    repeat(difficulty.startTiles) { board = addRandomTile(board) }
    return board
}

internal fun slideLeft(row: List<Int>): Pair<List<Int>, Int> {
    val tiles = row.filter { it != 0 }.toMutableList()
    var score = 0
    var i = 0
    while (i < tiles.size - 1) {
        if (tiles[i] == tiles[i + 1]) {
            tiles[i] *= 2
            score += tiles[i]
            tiles.removeAt(i + 1)
        }
        i++
    }
    return (tiles + List(4 - tiles.size) { 0 }) to score
}

internal fun transpose2048(b: Board2048): Board2048 = List(4) { r -> List(4) { c -> b[c][r] } }

internal fun applyMove2048(dir: Dir2048, board: Board2048): Pair<Board2048, Int> = when (dir) {
    Dir2048.Left -> {
        var score = 0
        board.map { row -> slideLeft(row).also { score += it.second }.first } to score
    }
    Dir2048.Right -> {
        var score = 0
        board.map { row -> slideLeft(row.reversed()).also { score += it.second }.first.reversed() } to score
    }
    Dir2048.Up -> {
        val (moved, score) = applyMove2048(Dir2048.Left, transpose2048(board))
        transpose2048(moved) to score
    }
    Dir2048.Down -> {
        val (moved, score) = applyMove2048(Dir2048.Right, transpose2048(board))
        transpose2048(moved) to score
    }
}

internal fun hasValidMoves2048(board: Board2048): Boolean {
    if (board.any { row -> row.any { it == 0 } }) return true
    for (r in 0..3) for (c in 0..3) {
        if (c < 3 && board[r][c] == board[r][c + 1]) return true
        if (r < 3 && board[r][c] == board[r + 1][c]) return true
    }
    return false
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private fun tileBackground(value: Int): Color = when (value) {
    0    -> Color(0xFFCDC1B4)
    2    -> Color(0xFFEEE4DA)
    4    -> Color(0xFFEDE0C8)
    8    -> Color(0xFFF2B179)
    16   -> Color(0xFFF59563)
    32   -> Color(0xFFF67C5F)
    64   -> Color(0xFFF65E3B)
    128  -> Color(0xFFEDCF72)
    256  -> Color(0xFFEDCC61)
    512  -> Color(0xFFEDC850)
    1024 -> Color(0xFFEDC53F)
    2048 -> Color(0xFFEDC22E)
    else -> Color(0xFF3C3A32)
}

private fun tileTextColor(value: Int): Color =
    if (value <= 4) Color(0xFF776E65) else Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Game2048Screen(
    difficulty: TileMergeDifficulty = TileMergeDifficulty.Normal,
    onBack: () -> Unit,
    onBestScore: (tile: Int, score: Int) -> Unit = { _, _ -> }
) {
    var board by remember(difficulty) { mutableStateOf(startingBoard2048(difficulty)) }
    var score by remember { mutableIntStateOf(0) }
    var bestTile by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }

    fun move(dir: Dir2048) {
        if (gameOver) return
        val (newBoard, gained) = applyMove2048(dir, board)
        if (newBoard == board) return
        val next = addRandomTile(newBoard)
        board = next
        score += gained
        val maxTile = next.maxOf { row -> row.maxOrNull() ?: 0 }
        if (maxTile > bestTile) bestTile = maxTile
        when {
            next.any { row -> row.any { it == difficulty.targetTile } } -> won = true
            !hasValidMoves2048(next) -> gameOver = true
        }
    }

    fun reset() {
        board = startingBoard2048(difficulty)
        score = 0
        bestTile = 0
        gameOver = false
        won = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tile Merge • ${difficulty.label}") },
                navigationIcon = {
                    IconButton(onClick = {
                        onBestScore(bestTile, score)
                        onBack()
                    }) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Score: $score", style = MaterialTheme.typography.titleMedium)
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(8.dp))

            if (won) Text("You reached ${difficulty.targetTile}!", color = Color(0xFF776E65), fontWeight = FontWeight.Bold)
            if (gameOver) Text("Game Over!", color = Color.Red, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFBBADA0), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .pointerInput(board) {
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
                                val threshold = 40f
                                when {
                                    abs(dx) > abs(dy) && abs(dx) > threshold ->
                                        move(if (dx > 0) Dir2048.Right else Dir2048.Left)
                                    abs(dy) > threshold ->
                                        move(if (dy > 0) Dir2048.Down else Dir2048.Up)
                                }
                            }
                        )
                    }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    board.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            row.forEach { value ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(tileBackground(value), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (value != 0) {
                                        val fontSize = when {
                                            value < 100  -> 26.sp
                                            value < 1000 -> 20.sp
                                            else         -> 15.sp
                                        }
                                        Text(
                                            text = value.toString(),
                                            color = tileTextColor(value),
                                            fontSize = fontSize,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Swipe to move every tile. Match equal numbers to merge. Target: ${difficulty.targetTile}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
