package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — Klondike (draw 1, unlimited redeals)
// ---------------------------------------------------------------------------

internal data class SolitaireState(
    val stock: List<PlayingCard>,
    val waste: List<PlayingCard>,
    val foundations: List<List<PlayingCard>>, // 4, by suit order of first ace placed
    val tableau: List<List<PlayingCard>>,     // 7 piles
    val faceUpCounts: List<Int>               // per tableau pile
) {
    val won: Boolean get() = foundations.sumOf { it.size } == 52
}

internal fun newSolitaire(random: Random = Random.Default): SolitaireState {
    val deck = shuffledDeck(random).toMutableList()
    val tableau = (0 until 7).map { i -> List(i + 1) { deck.removeAt(0) } }
    return SolitaireState(
        stock = deck,
        waste = emptyList(),
        foundations = List(4) { emptyList() },
        tableau = tableau,
        faceUpCounts = List(7) { 1 }
    )
}

internal fun solitaireDraw(s: SolitaireState): SolitaireState = when {
    s.stock.isNotEmpty() -> s.copy(stock = s.stock.drop(1), waste = s.waste + s.stock.first())
    s.waste.isNotEmpty() -> s.copy(stock = s.waste.reversed(), waste = emptyList())
    else -> s
}

internal fun canGoOnFoundation(card: PlayingCard, pile: List<PlayingCard>): Boolean =
    if (pile.isEmpty()) card.rank == 1
    else pile.last().suit == card.suit && pile.last().rank + 1 == card.rank

internal fun canGoOnTableau(card: PlayingCard, pile: List<PlayingCard>): Boolean =
    if (pile.isEmpty()) card.rank == 13
    else pile.last().suit.isRed != card.suit.isRed && pile.last().rank == card.rank + 1

/** Moves the waste's top card to a foundation or tableau pile, if possible. */
internal fun moveWasteCard(s: SolitaireState): SolitaireState? {
    val card = s.waste.lastOrNull() ?: return null
    s.foundations.forEachIndexed { i, pile ->
        if (canGoOnFoundation(card, pile)) {
            return s.copy(
                waste = s.waste.dropLast(1),
                foundations = s.foundations.mapIndexed { j, p -> if (j == i) p + card else p }
            )
        }
    }
    s.tableau.forEachIndexed { i, pile ->
        if (canGoOnTableau(card, pile)) {
            return s.copy(
                waste = s.waste.dropLast(1),
                tableau = s.tableau.mapIndexed { j, p -> if (j == i) p + card else p },
                faceUpCounts = s.faceUpCounts.mapIndexed { j, n -> if (j == i) n + 1 else n }
            )
        }
    }
    return null
}

/**
 * Moves the face-up run starting at [cardIndex] in pile [from]. Single top cards
 * may go to a foundation; runs move between tableau piles.
 */
internal fun moveTableauRun(s: SolitaireState, from: Int, cardIndex: Int): SolitaireState? {
    val pile = s.tableau[from]
    val faceUpStart = pile.size - s.faceUpCounts[from]
    if (cardIndex < faceUpStart || cardIndex >= pile.size) return null
    val run = pile.subList(cardIndex, pile.size)

    if (run.size == 1) {
        val card = run.first()
        s.foundations.forEachIndexed { i, fPile ->
            if (canGoOnFoundation(card, fPile)) {
                return removeRun(s, from, cardIndex).copy(
                    foundations = s.foundations.mapIndexed { j, p -> if (j == i) p + card else p }
                )
            }
        }
    }
    s.tableau.forEachIndexed { i, tPile ->
        if (i != from && canGoOnTableau(run.first(), tPile)) {
            val removed = removeRun(s, from, cardIndex)
            return removed.copy(
                tableau = removed.tableau.mapIndexed { j, p -> if (j == i) p + run else p },
                faceUpCounts = removed.faceUpCounts.mapIndexed { j, n -> if (j == i) n + run.size else n }
            )
        }
    }
    return null
}

private fun removeRun(s: SolitaireState, from: Int, cardIndex: Int): SolitaireState {
    val pile = s.tableau[from]
    val removedCount = pile.size - cardIndex
    val newPile = pile.subList(0, cardIndex)
    var newFaceUp = s.faceUpCounts[from] - removedCount
    if (newFaceUp <= 0) newFaceUp = if (newPile.isEmpty()) 0 else 1 // flip next card
    return s.copy(
        tableau = s.tableau.mapIndexed { j, p -> if (j == from) newPile else p },
        faceUpCounts = s.faceUpCounts.mapIndexed { j, n -> if (j == from) newFaceUp else n }
    )
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@Composable
private fun CardFace(card: PlayingCard?, faceUp: Boolean, highlighted: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(60.dp)
            .background(
                when {
                    card == null -> Color(0xFF1B5E20).copy(alpha = 0.35f)
                    !faceUp -> Color(0xFF37474F)
                    else -> Color.White
                },
                RoundedCornerShape(6.dp)
            )
            .border(
                2.dp,
                if (highlighted) Color(0xFFFFD54F) else Color.Black.copy(alpha = 0.25f),
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (card != null && faceUp) {
            Text(card.display, color = card.color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        } else if (card != null) {
            Text("✦", color = Color(0xFF90A4AE))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolitaireScreen(
    onBack: () -> Unit,
    onWin: (moves: Int) -> Unit = {}
) {
    var state by remember { mutableStateOf(newSolitaire()) }
    var moves by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) } // (pile, cardIndex)
    var reported by remember { mutableStateOf(false) }

    fun reset() {
        state = newSolitaire()
        moves = 0
        selected = null
        reported = false
    }

    fun afterMove(next: SolitaireState?) {
        if (next == null) return
        state = next
        moves++
        selected = null
        if (next.won && !reported) {
            reported = true
            onWin(moves)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solitaire") },
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
                .background(Color(0xFF2E7D32))
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Moves: $moves", color = Color.White, fontWeight = FontWeight.Bold)
                if (state.won) Text("You won! 🎉", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("New Deal") }
            }

            Spacer(Modifier.height(10.dp))

            // Top row: stock, waste, spacer, 4 foundations
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CardFace(card = state.stock.lastOrNull(), faceUp = false) {
                    state = solitaireDraw(state)
                    selected = null
                }
                CardFace(card = state.waste.lastOrNull(), faceUp = true) {
                    if (state.waste.isNotEmpty()) afterMove(moveWasteCard(state))
                }
                Spacer(Modifier.width(18.dp))
                state.foundations.forEach { pile ->
                    CardFace(card = pile.lastOrNull(), faceUp = true)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Tableau: 7 columns of overlapping cards
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.tableau.forEachIndexed { pileIdx, pile ->
                    Column(verticalArrangement = Arrangement.spacedBy((-38).dp)) {
                        if (pile.isEmpty()) {
                            CardFace(card = null, faceUp = false) {
                                val sel = selected
                                if (sel != null) afterMove(moveTableauRun(state, sel.first, sel.second))
                            }
                        }
                        val faceUpStart = pile.size - state.faceUpCounts[pileIdx]
                        pile.forEachIndexed { cardIdx, card ->
                            val faceUp = cardIdx >= faceUpStart
                            CardFace(
                                card = card,
                                faceUp = faceUp,
                                highlighted = selected == pileIdx to cardIdx
                            ) {
                                if (!faceUp) return@CardFace
                                val sel = selected
                                if (sel == null || sel.first == pileIdx) {
                                    // Select, or try auto-move on second tap of same card
                                    if (sel == pileIdx to cardIdx) {
                                        afterMove(moveTableauRun(state, pileIdx, cardIdx))
                                    } else {
                                        selected = pileIdx to cardIdx
                                    }
                                } else {
                                    // Try moving previously selected run onto this pile's tail
                                    val attempt = moveTableauRun(state, sel.first, sel.second)
                                    if (attempt != null) afterMove(attempt) else selected = pileIdx to cardIdx
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tap the deck to draw. Tap a card to select, tap again to auto-move it (foundation first, then any valid column). Kings fill empty columns.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}
