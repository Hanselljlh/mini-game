package net.sclan.minigames.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — classic Ludo on the standard 15×15 cross board.
// Token positions are relative: -1 = yard, 0..50 = main track (own start = 0),
// 51..55 = home column, 56 = home (done, exact roll required).
// ---------------------------------------------------------------------------

internal const val LUDO_DONE = 56

/** The 52 main-track cells in play order, as (col, row) on the 15×15 grid. */
internal val LUDO_TRACK: List<Pair<Int, Int>> = buildList {
    (1..5).forEach { add(it to 6) }        // 0-4   left arm, eastward
    (5 downTo 0).forEach { add(6 to it) }  // 5-10  up the left of top arm
    add(7 to 0)                            // 11    top center
    (0..5).forEach { add(8 to it) }        // 12-17 down the right of top arm
    (9..14).forEach { add(it to 6) }       // 18-23 right arm, eastward
    add(14 to 7)                           // 24    right center
    (14 downTo 9).forEach { add(it to 8) } // 25-30 right arm, westward
    (9..14).forEach { add(8 to it) }       // 31-36 down bottom arm
    add(7 to 14)                           // 37    bottom center
    (14 downTo 9).forEach { add(6 to it) } // 38-43 up bottom arm
    (5 downTo 0).forEach { add(it to 8) }  // 44-49 left arm, westward
    add(0 to 7)                            // 50    left center
    add(0 to 6)                            // 51    final cell before red start
}

internal val LUDO_START = listOf(0, 13, 26, 39) // track indices where each player enters
internal val LUDO_SAFE = setOf(0, 8, 13, 21, 26, 34, 39, 47) // starts + stars

/** Home column cells for each player, rel 51..55 (the 6th step is the center). */
internal val LUDO_HOME_COLS: List<List<Pair<Int, Int>>> = listOf(
    (1..5).map { it to 7 },          // player 0 (red, from left)
    (1..5).map { 7 to it },          // player 1 (green, from top)
    (13 downTo 9).map { it to 7 },   // player 2 (yellow, from right)
    (13 downTo 9).map { 7 to it }    // player 3 (blue, from bottom)
)

internal val LUDO_YARDS: List<List<Pair<Int, Int>>> = listOf(
    listOf(2 to 2, 2 to 3, 3 to 2, 3 to 3),       // red: top-left
    listOf(11 to 2, 11 to 3, 12 to 2, 12 to 3),   // green: top-right
    listOf(11 to 11, 11 to 12, 12 to 11, 12 to 12), // yellow: bottom-right
    listOf(2 to 11, 2 to 12, 3 to 11, 3 to 12)    // blue: bottom-left
)

internal data class LudoState(
    val playerCount: Int,
    val tokens: List<List<Int>>, // playerCount × 4 relative positions
    val turn: Int = 0,
    val dice: Int? = null,
    val message: String = "Roll to start!"
) {
    fun finished(player: Int): Boolean = tokens[player].all { it == LUDO_DONE }
    val winner: Int? get() = (0 until playerCount).firstOrNull { finished(it) }
}

internal fun newLudo(playerCount: Int): LudoState =
    LudoState(playerCount = playerCount, tokens = List(playerCount) { List(4) { -1 } })

internal fun ludoAbsCell(player: Int, rel: Int): Int = (LUDO_START[player] + rel) % 52

internal fun ludoLegalTokens(state: LudoState, dice: Int): List<Int> {
    val hand = state.tokens[state.turn]
    return hand.indices.filter { i ->
        val rel = hand[i]
        when {
            rel == -1 -> dice == 6
            rel + dice > LUDO_DONE -> false
            else -> true
        }
    }
}

/** Applies moving token [tokenIdx] by [dice]. Returns new state (turn advanced unless a 6 was rolled). */
internal fun ludoApply(state: LudoState, tokenIdx: Int, dice: Int): LudoState {
    val player = state.turn
    val rel = state.tokens[player][tokenIdx]
    val newRel = if (rel == -1) 0 else rel + dice
    var tokens = state.tokens.mapIndexed { p, hand ->
        if (p == player) hand.mapIndexed { i, r -> if (i == tokenIdx) newRel else r } else hand
    }
    var message = ""
    // Captures only on the shared main track, never on safe cells
    if (newRel in 0..50) {
        val landed = ludoAbsCell(player, newRel)
        if (landed !in LUDO_SAFE) {
            tokens = tokens.mapIndexed { p, hand ->
                if (p == player) hand
                else hand.map { r ->
                    if (r in 0..50 && ludoAbsCell(p, r) == landed) {
                        message = "Captured!"
                        -1
                    } else r
                }
            }
        }
    }
    if (newRel == LUDO_DONE) message = "Token home!"
    val extraTurn = dice == 6
    val nextTurn = if (extraTurn) player else (player + 1) % state.playerCount
    return state.copy(
        tokens = tokens,
        turn = nextTurn,
        dice = null,
        message = message.ifEmpty { if (extraTurn) "Rolled a 6 — go again!" else "" }
    )
}

/** Bot picks: finish > capture > enter > furthest along. */
internal fun ludoBotPick(state: LudoState, dice: Int): Int? {
    val legal = ludoLegalTokens(state, dice)
    if (legal.isEmpty()) return null
    val hand = state.tokens[state.turn]
    legal.firstOrNull { hand[it] != -1 && hand[it] + dice == LUDO_DONE }?.let { return it }
    legal.firstOrNull { i ->
        val rel = hand[i]
        val newRel = if (rel == -1) 0 else rel + dice
        newRel in 0..50 && ludoAbsCell(state.turn, newRel) !in LUDO_SAFE &&
            state.tokens.withIndex().any { (p, h) ->
                p != state.turn && h.any { r -> r in 0..50 && ludoAbsCell(p, r) == ludoAbsCell(state.turn, newRel) }
            }
    }?.let { return it }
    legal.firstOrNull { hand[it] == -1 }?.let { return it }
    return legal.maxByOrNull { hand[it] }
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

internal val ludoColors = listOf(Color(0xFFD32F2F), Color(0xFF388E3C), Color(0xFFFBC02D), Color(0xFF1976D2))
private val ludoNames = listOf("Red", "Green", "Yellow", "Blue")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoScreen(
    mode: LudoMode = LudoMode.VsBot,
    onBack: () -> Unit,
    onFinished: (playerWon: Boolean) -> Unit = {}
) {
    var state by remember(mode) { mutableStateOf(newLudo(mode.players)) }
    var reported by remember(mode) { mutableStateOf(false) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val random = remember(mode) { Random(System.currentTimeMillis()) }

    val isBotTurn = mode == LudoMode.VsBot && state.turn == 1 && state.winner == null

    fun finishIfWon() {
        val w = state.winner
        if (w != null && !reported) {
            reported = true
            onFinished(w == 0 || mode != LudoMode.VsBot)
        }
    }

    fun roll() {
        if (state.dice != null || state.winner != null) return
        val d = random.nextInt(1, 7)
        val legal = ludoLegalTokens(state.copy(dice = d), d)
        state = if (legal.isEmpty()) {
            state.copy(
                dice = null,
                turn = (state.turn + 1) % state.playerCount,
                message = "Rolled $d — no moves. ${ludoNames[(state.turn + 1) % state.playerCount]}'s turn."
            )
        } else {
            state.copy(dice = d, message = "Rolled $d — pick a token.")
        }
    }

    fun moveToken(tokenIdx: Int) {
        val d = state.dice ?: return
        if (tokenIdx !in ludoLegalTokens(state, d)) return
        state = ludoApply(state, tokenIdx, d)
        finishIfWon()
    }

    // Bot loop
    LaunchedEffect(isBotTurn, state.dice) {
        if (isBotTurn && state.dice == null) {
            delay(600)
            val d = random.nextInt(1, 7)
            val withDice = state.copy(dice = d)
            val pick = ludoBotPick(withDice, d)
            state = if (pick == null) {
                state.copy(turn = 0, message = "Bot rolled $d — no moves. Your turn.")
            } else {
                ludoApply(withDice, pick, d).let {
                    if (it.turn == 1 && it.message.isEmpty()) it.copy(message = "Bot rolled $d.") else it
                }
            }
            finishIfWon()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ludo • ${mode.label}") },
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
                val w = state.winner
                Text(
                    when {
                        w != null -> "${ludoNames[w]} wins! 🏆"
                        else -> "${ludoNames[state.turn]}'s turn"
                    },
                    fontWeight = FontWeight.Bold,
                    color = ludoColors[state.winner ?: state.turn]
                )
                Button(onClick = { state = newLudo(mode.players); reported = false }) { Text("New Game") }
            }
            if (state.message.isNotEmpty()) {
                Text(state.message, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onSizeChanged { boardSize = it }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cell = this.size.width / 15f
                    fun cellRect(col: Int, row: Int, color: Color) {
                        drawRect(color, topLeft = Offset(col * cell, row * cell), size = Size(cell, cell))
                        drawRect(
                            Color.Black.copy(alpha = 0.15f),
                            topLeft = Offset(col * cell, row * cell),
                            size = Size(cell, cell),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f)
                        )
                    }
                    // Yard quadrants (inactive players drawn faintly)
                    val yardRects = listOf(0 to 0, 9 to 0, 9 to 9, 0 to 9)
                    yardRects.forEachIndexed { p, (cx, cy) ->
                        drawRect(
                            ludoColors[p].copy(alpha = if (p < mode.players) 0.75f else 0.15f),
                            topLeft = Offset(cx * cell, cy * cell),
                            size = Size(6 * cell, 6 * cell)
                        )
                        drawRect(
                            Color.White,
                            topLeft = Offset((cx + 1.5f) * cell, (cy + 1.5f) * cell),
                            size = Size(3 * cell, 3 * cell)
                        )
                    }
                    // Track
                    LUDO_TRACK.forEachIndexed { idx, (col, row) ->
                        val startOwner = LUDO_START.indexOf(idx)
                        val color = when {
                            startOwner >= 0 -> ludoColors[startOwner].copy(alpha = 0.8f)
                            idx in LUDO_SAFE -> Color(0xFFBDBDBD)
                            else -> Color.White
                        }
                        cellRect(col, row, color)
                    }
                    // Home columns
                    LUDO_HOME_COLS.forEachIndexed { p, cells ->
                        cells.forEach { (col, row) -> cellRect(col, row, ludoColors[p].copy(alpha = 0.55f)) }
                    }
                    // Center
                    drawRect(Color(0xFF8D6E63), topLeft = Offset(6 * cell, 6 * cell), size = Size(3 * cell, 3 * cell))
                }

                // Tokens as tappable overlays
                if (boardSize.width > 0) {
                    val cellPx = boardSize.width / 15
                    val tokenDp = with(androidx.compose.ui.platform.LocalDensity.current) { (cellPx * 3 / 4).toDp() }
                    val d = state.dice
                    val legal = if (d != null) ludoLegalTokens(state, d) else emptyList()
                    for (p in 0 until state.playerCount) {
                        state.tokens[p].forEachIndexed { i, rel ->
                            val (col, row) = when {
                                rel == -1 -> LUDO_YARDS[p][i]
                                rel in 0..50 -> LUDO_TRACK[ludoAbsCell(p, rel)]
                                rel in 51..55 -> LUDO_HOME_COLS[p][rel - 51]
                                else -> 7 to 7 // done: center
                            }
                            val canMove = p == state.turn && !isBotTurn && i in legal
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(col * cellPx + cellPx / 8, row * cellPx + cellPx / 8) }
                                    .size(tokenDp)
                                    .background(ludoColors[p], CircleShape)
                                    .border(
                                        3.dp,
                                        if (canMove) Color.White else Color.Black.copy(alpha = 0.4f),
                                        CircleShape
                                    )
                                    .clickable(enabled = canMove) { moveToken(i) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.White, MaterialTheme.shapes.medium)
                        .border(2.dp, Color.Black.copy(alpha = 0.4f), MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.dice?.toString() ?: "?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
                Button(
                    onClick = ::roll,
                    enabled = state.dice == null && !isBotTurn && state.winner == null
                ) { Text("Roll") }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Roll a 6 to leave the yard. Land on an opponent to send them home — grey stars are safe. Exact roll to finish. 6 rolls again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
