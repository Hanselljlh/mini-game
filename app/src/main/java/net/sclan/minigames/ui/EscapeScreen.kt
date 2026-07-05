package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// ---------------------------------------------------------------------------
// Pure game logic (Rush-Hour-style block escape on a 6×6 grid)
// The red block (id 0) is horizontal on row 2 and must reach the right edge.
// ---------------------------------------------------------------------------

internal const val ESC_SIZE = 6
internal const val ESC_EXIT_ROW = 2

internal data class EscBlock(val id: Int, val row: Int, val col: Int, val len: Int, val horizontal: Boolean) {
    fun cells(): List<Pair<Int, Int>> =
        (0 until len).map { i -> if (horizontal) row to (col + i) else (row + i) to col }
}

internal fun escOccupied(blocks: List<EscBlock>, except: Int = -1): Set<Pair<Int, Int>> =
    blocks.filter { it.id != except }.flatMap { it.cells() }.toSet()

/** Moves a block one cell along its axis (delta = ±1). Returns null if blocked. */
internal fun escMove(blocks: List<EscBlock>, id: Int, delta: Int): List<EscBlock>? {
    val block = blocks.first { it.id == id }
    val moved = if (block.horizontal) block.copy(col = block.col + delta) else block.copy(row = block.row + delta)
    if (moved.cells().any { (r, c) -> r !in 0 until ESC_SIZE || c !in 0 until ESC_SIZE }) return null
    val others = escOccupied(blocks, except = id)
    if (moved.cells().any { it in others }) return null
    return blocks.map { if (it.id == id) moved else it }
}

internal fun escSolved(blocks: List<EscBlock>): Boolean {
    val red = blocks.first { it.id == 0 }
    return red.col + red.len >= ESC_SIZE
}

/** Handcrafted original levels: red block first (row 2, horizontal). */
internal val ESCAPE_LEVELS: List<List<EscBlock>> = listOf(
    // 1 — one mover
    listOf(
        EscBlock(0, 2, 0, 2, true),
        EscBlock(1, 0, 2, 3, false)
    ),
    // 2 — two movers
    listOf(
        EscBlock(0, 2, 1, 2, true),
        EscBlock(1, 1, 3, 2, false),
        EscBlock(2, 0, 4, 3, false)
    ),
    // 3 — slide up out of the lane
    listOf(
        EscBlock(0, 2, 0, 2, true),
        EscBlock(1, 2, 2, 2, false),
        EscBlock(2, 0, 3, 2, false),
        EscBlock(3, 4, 3, 2, true)
    ),
    // 4 — quick unhook near the exit
    listOf(
        EscBlock(0, 2, 3, 2, true),
        EscBlock(1, 1, 5, 2, false),
        EscBlock(2, 4, 0, 3, true),
        EscBlock(3, 0, 0, 2, false)
    ),
    // 5 — two blockers, both must clear
    listOf(
        EscBlock(0, 2, 0, 2, true),
        EscBlock(1, 2, 2, 2, false),
        EscBlock(2, 2, 4, 3, false),
        EscBlock(3, 0, 4, 2, true),
        EscBlock(4, 0, 2, 2, false)
    ),
    // 6 — long pole and a tail
    listOf(
        EscBlock(0, 2, 1, 2, true),
        EscBlock(1, 1, 3, 3, false),
        EscBlock(2, 2, 4, 2, false),
        EscBlock(3, 0, 0, 2, false),
        EscBlock(4, 4, 0, 3, true)
    )
)

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val escBlockColors = listOf(
    Color(0xFFE53935), // red hero
    Color(0xFF8D6E63), Color(0xFF5C6BC0), Color(0xFF26A69A),
    Color(0xFFFFB300), Color(0xFF7E57C2), Color(0xFF66BB6A)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscapeScreen(
    pack: EscapePack = EscapePack.Rookie,
    onBack: () -> Unit,
    onLevelDone: (levelIndex: Int, moves: Int) -> Unit = { _, _ -> }
) {
    var levelInPack by remember(pack) { mutableIntStateOf(0) }
    val levelIndex = pack.firstLevel + levelInPack
    var blocks by remember(pack, levelInPack) { mutableStateOf(ESCAPE_LEVELS[levelIndex]) }
    var selected by remember(pack, levelInPack) { mutableIntStateOf(0) }
    var moves by remember(pack, levelInPack) { mutableIntStateOf(0) }
    var won by remember(pack, levelInPack) { mutableStateOf(false) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }

    fun reset() {
        blocks = ESCAPE_LEVELS[levelIndex]
        selected = 0
        moves = 0
        won = false
    }

    fun trySlide(dx: Int, dy: Int) {
        if (won) return
        val block = blocks.first { it.id == selected }
        val delta = if (block.horizontal) dx else dy
        if (delta == 0) return
        val next = escMove(blocks, selected, delta) ?: return
        blocks = next
        moves++
        if (escSolved(next)) {
            won = true
            onLevelDone(levelIndex, moves)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escape • ${pack.label} ${levelInPack + 1}/${pack.count}") },
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
                Text("Moves: $moves", style = MaterialTheme.typography.titleMedium)
                if (won) Text("Escaped! 🚗", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("Reset") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .onSizeChanged { boardSize = it }
                    .pointerInput(pack, levelInPack, selected, won) {
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
                                val threshold = 25f
                                when {
                                    abs(dx) > abs(dy) && abs(dx) > threshold -> trySlide(if (dx > 0) 1 else -1, 0)
                                    abs(dy) > threshold -> trySlide(0, if (dy > 0) 1 else -1)
                                }
                            }
                        )
                    }
            ) {
                blocks.forEach { block ->
                    val w = if (block.horizontal) block.len else 1
                    val h = if (block.horizontal) 1 else block.len
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    boardSize.width * block.col / ESC_SIZE,
                                    boardSize.height * block.row / ESC_SIZE
                                )
                            }
                            .fillMaxWidth(w.toFloat() / ESC_SIZE)
                            .aspectRatio(w.toFloat() / h)
                            .padding(3.dp)
                            .background(
                                escBlockColors[block.id % escBlockColors.size],
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                3.dp,
                                if (selected == block.id) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selected = block.id }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (won) {
                if (levelInPack + 1 < pack.count) {
                    Button(onClick = { levelInPack++ }) { Text("Next Level") }
                } else {
                    Text("Pack complete! 🏁", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                "Tap a block to select it, then swipe to slide it along its track. Get the red block to the right edge.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

