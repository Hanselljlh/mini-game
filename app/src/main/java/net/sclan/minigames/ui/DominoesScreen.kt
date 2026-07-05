package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — double-six block dominoes vs bot, draw from boneyard
// ---------------------------------------------------------------------------

internal data class Domino(val a: Int, val b: Int) {
    fun has(pip: Int): Boolean = a == pip || b == pip
    fun other(pip: Int): Int = if (a == pip) b else a
}

internal fun dominoSet(): List<Domino> =
    (0..6).flatMap { a -> (a..6).map { b -> Domino(a, b) } }

internal data class DominoState(
    val chain: List<Domino> = emptyList(), // oriented: chain[i].b matches chain[i+1].a
    val playerHand: List<Domino>,
    val botHand: List<Domino>,
    val boneyard: List<Domino>,
    val playerTurn: Boolean = true,
    val message: String = "Your move."
) {
    val leftEnd: Int? get() = chain.firstOrNull()?.a
    val rightEnd: Int? get() = chain.lastOrNull()?.b
    val finished: Boolean get() = playerHand.isEmpty() || botHand.isEmpty()
}

internal fun newDominoes(random: Random = Random.Default): DominoState {
    val tiles = dominoSet().shuffled(random)
    return DominoState(
        playerHand = tiles.take(7),
        botHand = tiles.drop(7).take(7),
        boneyard = tiles.drop(14)
    )
}

internal fun dominoPlayable(state: DominoState, tile: Domino): Boolean =
    state.chain.isEmpty() || tile.has(state.leftEnd!!) || tile.has(state.rightEnd!!)

/** Plays [tile] on the left or right end (oriented correctly). Returns null if illegal. */
internal fun playDomino(state: DominoState, tile: Domino, fromPlayer: Boolean, preferLeft: Boolean): DominoState? {
    val hand = if (fromPlayer) state.playerHand else state.botHand
    if (tile !in hand) return null
    val newChain: List<Domino> = when {
        state.chain.isEmpty() -> listOf(tile)
        preferLeft && tile.has(state.leftEnd!!) ->
            listOf(Domino(tile.other(state.leftEnd!!), state.leftEnd!!)) + state.chain
        tile.has(state.rightEnd ?: -1) ->
            state.chain + listOf(Domino(state.rightEnd!!, tile.other(state.rightEnd!!)))
        tile.has(state.leftEnd ?: -1) ->
            listOf(Domino(tile.other(state.leftEnd!!), state.leftEnd!!)) + state.chain
        else -> return null
    }
    return state.copy(
        chain = newChain,
        playerHand = if (fromPlayer) state.playerHand - tile else state.playerHand,
        botHand = if (!fromPlayer) state.botHand - tile else state.botHand,
        playerTurn = !fromPlayer
    )
}

/** Draw one tile for the side to move; null if boneyard is empty. */
internal fun drawDomino(state: DominoState, forPlayer: Boolean): DominoState? {
    val tile = state.boneyard.firstOrNull() ?: return null
    return state.copy(
        boneyard = state.boneyard.drop(1),
        playerHand = if (forPlayer) state.playerHand + tile else state.playerHand,
        botHand = if (!forPlayer) state.botHand + tile else state.botHand
    )
}

internal fun dominoPipCount(hand: List<Domino>): Int = hand.sumOf { it.a + it.b }

/** Bot: play first playable (highest pips first); draw until playable or pass. */
internal fun botDominoTurn(state: DominoState): DominoState {
    var s = state
    while (true) {
        val playable = s.botHand.sortedByDescending { it.a + it.b }.firstOrNull { dominoPlayable(s, it) }
        if (playable != null) {
            return playDomino(s, playable, fromPlayer = false, preferLeft = false)
                ?.copy(message = "Bot played ${playable.a}|${playable.b}. Your move.")
                ?: s.copy(playerTurn = true)
        }
        val drawn = drawDomino(s, forPlayer = false) ?: return s.copy(playerTurn = true, message = "Bot passes. Your move.")
        s = drawn
    }
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@Composable
private fun DominoTile(tile: Domino, highlighted: Boolean = false, enabled: Boolean = true, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .border(
                2.dp,
                if (highlighted) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp)
            )
            .background(if (enabled) Color(0xFFFFF8E1) else Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(tile.a.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF37474F))
        Box(Modifier.width(2.dp).height(20.dp).background(Color(0xFF90A4AE)).padding(horizontal = 3.dp))
        Text(tile.b.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF37474F))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DominoesScreen(
    onBack: () -> Unit,
    onFinished: (playerWon: Boolean) -> Unit = {}
) {
    var state by remember { mutableStateOf(newDominoes()) }
    var reported by remember { mutableStateOf(false) }

    fun finishIfNeeded() {
        if (state.finished && !reported) {
            reported = true
            onFinished(state.playerHand.isEmpty())
        }
    }

    fun playerPlay(tile: Domino) {
        if (!state.playerTurn || state.finished) return
        val next = playDomino(state, tile, fromPlayer = true, preferLeft = false) ?: return
        state = next
        finishIfNeeded()
        if (!state.finished) {
            state = botDominoTurn(state)
            finishIfNeeded()
        }
    }

    fun playerDrawOrPass() {
        if (!state.playerTurn || state.finished) return
        val canPlay = state.playerHand.any { dominoPlayable(state, it) }
        if (canPlay) return
        val drawn = drawDomino(state, forPlayer = true)
        if (drawn != null) {
            state = drawn.copy(message = "Drew a tile.")
        } else {
            state = botDominoTurn(state.copy(playerTurn = false, message = "You pass."))
            finishIfNeeded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dominoes") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bot: ${state.botHand.size} tiles • Boneyard: ${state.boneyard.size}", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { state = newDominoes(); reported = false }) { Text("New Game") }
            }

            Spacer(Modifier.height(10.dp))

            if (state.finished) {
                Text(
                    if (state.playerHand.isEmpty()) "Domino! You win! 🎉"
                    else "Bot went out — bot wins. (You held ${dominoPipCount(state.playerHand)} pips.)",
                    fontWeight = FontWeight.Bold,
                    color = if (state.playerHand.isEmpty()) Color(0xFF2E7D32) else Color.Red
                )
            } else {
                Text(state.message, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            // Chain
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF33691E).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.chain.isEmpty()) {
                    Text("Play any tile to start the chain", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.chain.forEach { tile -> DominoTile(tile, enabled = false) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Your hand — tap a highlighted tile to play it:", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.playerHand.forEach { tile ->
                    DominoTile(
                        tile,
                        highlighted = state.playerTurn && dominoPlayable(state, tile),
                        enabled = true
                    ) { playerPlay(tile) }
                }
            }

            Spacer(Modifier.height(12.dp))
            val stuck = state.playerTurn && !state.finished && state.playerHand.none { dominoPlayable(state, it) }
            OutlinedButton(onClick = ::playerDrawOrPass, enabled = stuck) {
                Text(if (state.boneyard.isEmpty()) "Pass" else "Draw from Boneyard")
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Match a tile to either end of the chain. Can't play? Draw until you can (or pass when the boneyard is empty). First to empty their hand wins.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
