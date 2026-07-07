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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Pure game logic (Kalah rules)
// Pits 0-5: player 1's row. Pit 6: player 1's store.
// Pits 7-12: player 2's row. Pit 13: player 2's store.
// ---------------------------------------------------------------------------

internal data class MancalaState(
    val pits: List<Int> = List(14) { if (it == 6 || it == 13) 0 else 4 },
    val turn: Int = 1,
    val finished: Boolean = false
) {
    val store1: Int get() = pits[6]
    val store2: Int get() = pits[13]
}

internal fun mancalaValidMoves(state: MancalaState): List<Int> {
    if (state.finished) return emptyList()
    val range = if (state.turn == 1) 0..5 else 7..12
    return range.filter { state.pits[it] > 0 }
}

/** Sows from [pit]; returns null for illegal moves. Handles extra turns, captures, and game end. */
internal fun sowMancala(state: MancalaState, pit: Int): MancalaState? {
    if (pit !in mancalaValidMoves(state)) return null
    val pits = state.pits.toMutableList()
    var seeds = pits[pit]
    pits[pit] = 0
    var index = pit
    val ownStore = if (state.turn == 1) 6 else 13
    val skipStore = if (state.turn == 1) 13 else 6

    while (seeds > 0) {
        index = (index + 1) % 14
        if (index == skipStore) continue
        pits[index]++
        seeds--
    }

    // Capture: last seed landed in an empty own-side pit → take it plus the opposite pit.
    val ownRange = if (state.turn == 1) 0..5 else 7..12
    if (index in ownRange && pits[index] == 1) {
        val opposite = 12 - index
        if (pits[opposite] > 0) {
            pits[ownStore] += pits[opposite] + 1
            pits[opposite] = 0
            pits[index] = 0
        }
    }

    val extraTurn = index == ownStore
    var next = MancalaState(
        pits = pits,
        turn = if (extraTurn) state.turn else if (state.turn == 1) 2 else 1
    )

    // Game over: one side empty → the other side sweeps its remaining seeds.
    val side1Empty = (0..5).all { next.pits[it] == 0 }
    val side2Empty = (7..12).all { next.pits[it] == 0 }
    if (side1Empty || side2Empty) {
        val swept = next.pits.toMutableList()
        swept[6] += (0..5).sumOf { swept[it] }
        swept[13] += (7..12).sumOf { swept[it] }
        for (i in 0..5) swept[i] = 0
        for (i in 7..12) swept[i] = 0
        next = next.copy(pits = swept, finished = true)
    }
    return next
}

/** Easy bot: take an extra-turn move if available, otherwise the pit with the most seeds. */
internal fun mancalaBotMove(state: MancalaState): Int? {
    val moves = mancalaValidMoves(state)
    if (moves.isEmpty()) return null
    moves.firstOrNull { m ->
        val after = sowMancala(state, m)
        after != null && !after.finished && after.turn == state.turn
    }?.let { return it }
    return moves.maxByOrNull { state.pits[it] }
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MancalaScreen(
    mode: MancalaMode = MancalaMode.TwoPlayer,
    onBack: () -> Unit,
    onFinished: (p1: Int, p2: Int) -> Unit = { _, _ -> }
) {
    var state by remember(mode) { mutableStateOf(MancalaState()) }
    var reported by remember(mode) { mutableStateOf(false) }

    fun reset() {
        state = MancalaState()
        reported = false
    }

    fun afterMove(next: MancalaState) {
        state = next
        if (next.finished && !reported) {
            reported = true
            onFinished(next.store1, next.store2)
            return
        }
        // Bot plays as player 2, chaining extra turns.
        if (mode == MancalaMode.EasyBot) {
            var cur = next
            while (!cur.finished && cur.turn == 2) {
                val botPit = mancalaBotMove(cur) ?: break
                cur = sowMancala(cur, botPit) ?: break
            }
            state = cur
            if (cur.finished && !reported) {
                reported = true
                onFinished(cur.store1, cur.store2)
            }
        }
    }

    fun tapPit(pit: Int) {
        if (mode == MancalaMode.EasyBot && state.turn != 1) return
        val next = sowMancala(state, pit) ?: return
        afterMove(next)
    }

    val p1Color = Color(0xFF8D6E63)
    val p2Color = Color(0xFF5D4037)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mancala • ${mode.label}") },
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
                val topName = if (mode == MancalaMode.EasyBot) "Bot" else "Player 2"
                if (state.finished) {
                    val msg = when {
                        state.store1 > state.store2 -> "${if (mode == MancalaMode.EasyBot) "You win" else "Player 1 wins"}! ${state.store1}–${state.store2}"
                        state.store2 > state.store1 -> "$topName wins! ${state.store2}–${state.store1}"
                        else -> "Draw ${state.store1}–${state.store2}"
                    }
                    Text(msg, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        if (state.turn == 1) (if (mode == MancalaMode.EasyBot) "Your turn" else "Player 1's turn")
                        else (if (mode == MancalaMode.EasyBot) "Bot thinking…" else "Player 2's turn"),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(20.dp))

            @Composable
            fun Pit(index: Int, enabled: Boolean, dark: Boolean) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (dark) p2Color.copy(alpha = if (enabled) 1f else 0.55f)
                            else p1Color.copy(alpha = if (enabled) 1f else 0.55f),
                            CircleShape
                        )
                        .clickable(enabled = enabled) { tapPit(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.pits[index].toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Player 2 store (left)
                Box(
                    modifier = Modifier.width(52.dp).height(120.dp).background(p2Color, RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.store2.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Player 2 row (12 → 7, right-to-left flow)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 12 downTo 7) {
                            Pit(i, enabled = !state.finished && state.turn == 2 && mode == MancalaMode.TwoPlayer && state.pits[i] > 0, dark = true)
                        }
                    }
                    // Player 1 row (0 → 5)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0..5) {
                            Pit(i, enabled = !state.finished && state.turn == 1 && state.pits[i] > 0, dark = false)
                        }
                    }
                }

                // Player 1 store (right)
                Box(
                    modifier = Modifier.width(52.dp).height(120.dp).background(p1Color, RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.store1.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Tap one of your pits to sow its seeds counter-clockwise. Land in your store for an extra turn; " +
                    "land in your empty pit to capture the seeds opposite. Most seeds banked wins.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
