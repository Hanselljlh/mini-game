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
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic (1010-style block placement, 10×10 board)
// ---------------------------------------------------------------------------

internal const val BF_SIZE = 10

/** Piece shapes as cell offsets from their top-left anchor. */
internal val BF_SHAPES: List<List<Pair<Int, Int>>> = listOf(
    listOf(0 to 0),                                                    // dot
    listOf(0 to 0, 0 to 1),                                            // 2 across
    listOf(0 to 0, 1 to 0),                                            // 2 down
    listOf(0 to 0, 0 to 1, 0 to 2),                                    // 3 across
    listOf(0 to 0, 1 to 0, 2 to 0),                                    // 3 down
    listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1),                            // square
    listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3),                            // 4 across
    listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0),                            // 4 down
    listOf(0 to 0, 1 to 0, 1 to 1),                                    // small L
    listOf(0 to 0, 0 to 1, 1 to 1),                                    // small J
    listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0, 2 to 0),                    // big L
    listOf(0 to 0, 0 to 1, 0 to 2, 1 to 2, 2 to 2),                    // big J
    listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 1, 1 to 2, 2 to 0, 2 to 1, 2 to 2) // 3×3
)

internal fun bfRandomPieces(count: Int = 3, random: Random = Random.Default): List<Int> =
    List(count) { random.nextInt(BF_SHAPES.size) }

internal fun bfCanPlace(board: Set<Pair<Int, Int>>, shape: List<Pair<Int, Int>>, anchorR: Int, anchorC: Int): Boolean =
    shape.all { (dr, dc) ->
        val r = anchorR + dr
        val c = anchorC + dc
        r in 0 until BF_SIZE && c in 0 until BF_SIZE && (r to c) !in board
    }

/** Places the shape then clears full rows and columns. Returns (board, cellsCleared). */
internal fun bfPlace(board: Set<Pair<Int, Int>>, shape: List<Pair<Int, Int>>, anchorR: Int, anchorC: Int): Pair<Set<Pair<Int, Int>>, Int> {
    val placed = board + shape.map { (dr, dc) -> (anchorR + dr) to (anchorC + dc) }
    val fullRows = (0 until BF_SIZE).filter { r -> (0 until BF_SIZE).all { c -> (r to c) in placed } }
    val fullCols = (0 until BF_SIZE).filter { c -> (0 until BF_SIZE).all { r -> (r to c) in placed } }
    val cleared = placed.filterNot { (r, c) -> r in fullRows || c in fullCols }.toSet()
    val cellsCleared = placed.size - cleared.size
    return cleared to cellsCleared
}

internal fun bfAnyPlacement(board: Set<Pair<Int, Int>>, shapeIdx: Int): Boolean {
    val shape = BF_SHAPES[shapeIdx]
    for (r in 0 until BF_SIZE) for (c in 0 until BF_SIZE) {
        if (bfCanPlace(board, shape, r, c)) return true
    }
    return false
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val bfPieceColors = listOf(
    Color(0xFF4FC3F7), Color(0xFF81C784), Color(0xFFFFB74D), Color(0xFFF06292),
    Color(0xFFBA68C8), Color(0xFF4DB6AC), Color(0xFFFFD54F)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockFillScreen(
    onBack: () -> Unit,
    onGameOver: (score: Int) -> Unit = {}
) {
    var board by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var pieces by remember { mutableStateOf(bfRandomPieces()) }
    var used by remember { mutableStateOf(setOf<Int>()) } // indices into pieces
    var selectedPiece by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var over by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }

    fun refillIfNeeded() {
        if (used.size == pieces.size) {
            pieces = bfRandomPieces()
            used = emptySet()
        }
    }

    fun checkGameOver() {
        val remaining = pieces.indices.filter { it !in used }
        if (remaining.isNotEmpty() && remaining.none { bfAnyPlacement(board, pieces[it]) }) {
            over = true
            if (!reported) {
                reported = true
                onGameOver(score)
            }
        }
    }

    fun reset() {
        board = emptySet()
        pieces = bfRandomPieces()
        used = emptySet()
        selectedPiece = null
        score = 0
        over = false
        reported = false
    }

    fun tapCell(r: Int, c: Int) {
        if (over) return
        val pieceIdx = selectedPiece ?: return
        val shape = BF_SHAPES[pieces[pieceIdx]]
        if (!bfCanPlace(board, shape, r, c)) return
        val (next, cleared) = bfPlace(board, shape, r, c)
        board = next
        score += shape.size + cleared * 2
        used = used + pieceIdx
        selectedPiece = null
        refillIfNeeded()
        checkGameOver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Block Fill") },
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
                Text("Score: $score", style = MaterialTheme.typography.titleMedium)
                if (over) Text("No room left!", color = Color.Red, fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(3.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (r in 0 until BF_SIZE) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until BF_SIZE) {
                                val filled = (r to c) in board
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.dp)
                                        .background(
                                            if (filled) Color(0xFF5C6BC0) else MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(3.dp)
                                        )
                                        .clickable { tapCell(r, c) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Piece tray
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pieces.forEachIndexed { idx, shapeIdx ->
                    val isUsed = idx in used
                    val shape = BF_SHAPES[shapeIdx]
                    val rows = shape.maxOf { it.first } + 1
                    val cols = shape.maxOf { it.second } + 1
                    Box(
                        modifier = Modifier
                            .border(
                                2.dp,
                                if (selectedPiece == idx) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(6.dp)
                            .clickable(enabled = !isUsed && !over) {
                                selectedPiece = if (selectedPiece == idx) null else idx
                            }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (r in 0 until rows) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    for (c in 0 until cols) {
                                        Box(
                                            Modifier
                                                .size(14.dp)
                                                .background(
                                                    when {
                                                        (r to c) !in shape -> Color.Transparent
                                                        isUsed -> Color.Gray.copy(alpha = 0.3f)
                                                        else -> bfPieceColors[shapeIdx % bfPieceColors.size]
                                                    },
                                                    RoundedCornerShape(2.dp)
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
                "Tap a piece, then tap the board square for its top-left corner. Full rows and columns clear for bonus points.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
