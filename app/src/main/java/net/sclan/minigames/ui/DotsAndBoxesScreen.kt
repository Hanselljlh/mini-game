package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
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
// Pure game logic
// Boxes grid is n×n. Horizontal edges: (n+1) rows × n cols, keyed (row, col).
// Vertical edges: n rows × (n+1) cols, keyed (row, col). Players are 1 and 2.
// ---------------------------------------------------------------------------

internal data class DotsState(
    val n: Int,
    val hEdges: Map<Pair<Int, Int>, Int> = emptyMap(), // (row, col) -> player who drew it
    val vEdges: Map<Pair<Int, Int>, Int> = emptyMap(),
    val boxes: Map<Pair<Int, Int>, Int> = emptyMap(),  // (row, col) -> owning player
    val turn: Int = 1
) {
    val scoreOf1: Int get() = boxes.values.count { it == 1 }
    val scoreOf2: Int get() = boxes.values.count { it == 2 }
    val finished: Boolean get() = boxes.size == n * n
}

internal fun boxComplete(state: DotsState, row: Int, col: Int): Boolean =
    (row to col) in state.hEdges &&        // top
        (row + 1 to col) in state.hEdges && // bottom
        (row to col) in state.vEdges &&     // left
        (row to col + 1) in state.vEdges    // right

/** Claims an edge; completing ≥1 box keeps the same player's turn. Returns null if already taken. */
internal fun claimEdge(state: DotsState, horizontal: Boolean, row: Int, col: Int): DotsState? {
    val key = row to col
    if (horizontal && key in state.hEdges) return null
    if (!horizontal && key in state.vEdges) return null
    val mid = if (horizontal) state.copy(hEdges = state.hEdges + (key to state.turn))
    else state.copy(vEdges = state.vEdges + (key to state.turn))

    // Only boxes adjacent to the new edge can have been completed.
    val candidates = if (horizontal) listOf(row - 1 to col, row to col) else listOf(row to col - 1, row to col)
    val newlyDone = candidates.filter { (r, c) ->
        r in 0 until state.n && c in 0 until state.n &&
            (r to c) !in mid.boxes && boxComplete(mid, r, c)
    }
    val withBoxes = mid.copy(boxes = mid.boxes + newlyDone.associateWith { state.turn })
    return if (newlyDone.isNotEmpty()) withBoxes
    else withBoxes.copy(turn = if (state.turn == 1) 2 else 1)
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val p1Color = Color(0xFFE53935)
private val p2Color = Color(0xFF1E88E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DotsAndBoxesScreen(
    size: DotsAndBoxesSize = DotsAndBoxesSize.Small,
    onBack: () -> Unit,
    onFinished: (p1Score: Int, p2Score: Int) -> Unit = { _, _ -> }
) {
    var state by remember(size) { mutableStateOf(DotsState(size.boxes)) }
    var reported by remember(size) { mutableStateOf(false) }

    fun reset() {
        state = DotsState(size.boxes)
        reported = false
    }

    fun edgeTap(horizontal: Boolean, row: Int, col: Int) {
        if (state.finished) return
        val next = claimEdge(state, horizontal, row, col) ?: return
        state = next
        if (next.finished && !reported) {
            reported = true
            onFinished(next.scoreOf1, next.scoreOf2)
        }
    }

    val n = size.boxes
    val dotSize = 12.dp
    val edgeLength = when (size) {
        DotsAndBoxesSize.Small -> 64.dp
        DotsAndBoxesSize.Medium -> 52.dp
        DotsAndBoxesSize.Large -> 44.dp
    }
    val edgeThickness = 12.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dots & Boxes • ${size.label}") },
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
                Text("Red ${state.scoreOf1}", color = p1Color, fontWeight = FontWeight.Bold)
                if (state.finished) {
                    val msg = when {
                        state.scoreOf1 > state.scoreOf2 -> "Red wins!"
                        state.scoreOf2 > state.scoreOf1 -> "Blue wins!"
                        else -> "Draw!"
                    }
                    Text(msg, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        if (state.turn == 1) "Red's turn" else "Blue's turn",
                        color = if (state.turn == 1) p1Color else p2Color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text("Blue ${state.scoreOf2}", color = p2Color, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = ::reset) { Text("New Game") }
            Spacer(Modifier.height(12.dp))

            fun edgeColor(owner: Int?): Color = when (owner) {
                1 -> p1Color
                2 -> p2Color
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            Column {
                for (displayRow in 0..2 * n) {
                    if (displayRow % 2 == 0) {
                        val er = displayRow / 2
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            for (c in 0 until n) {
                                Box(Modifier.size(dotSize).background(MaterialTheme.colorScheme.onSurface, CircleShape))
                                Box(
                                    Modifier
                                        .width(edgeLength)
                                        .height(edgeThickness)
                                        .padding(vertical = 2.dp)
                                        .background(edgeColor(state.hEdges[er to c]), RoundedCornerShape(3.dp))
                                        .clickable { edgeTap(true, er, c) }
                                )
                            }
                            Box(Modifier.size(dotSize).background(MaterialTheme.colorScheme.onSurface, CircleShape))
                        }
                    } else {
                        val br = displayRow / 2
                        Row {
                            for (c in 0..n) {
                                Box(
                                    Modifier
                                        .width(edgeThickness)
                                        .height(edgeLength)
                                        .padding(horizontal = 2.dp)
                                        .background(edgeColor(state.vEdges[br to c]), RoundedCornerShape(3.dp))
                                        .clickable { edgeTap(false, br, c) }
                                )
                                if (c < n) {
                                    val owner = state.boxes[br to c]
                                    Box(
                                        modifier = Modifier.size(edgeLength),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (owner != null) {
                                            Box(
                                                Modifier
                                                    .size(edgeLength - 12.dp)
                                                    .background(
                                                        (if (owner == 1) p1Color else p2Color).copy(alpha = 0.35f),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tap a gap between two dots to draw a line. Close the fourth side of a box to claim it and move again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
