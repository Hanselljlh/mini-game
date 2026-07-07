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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal data class MemoryCard(
    val index: Int,
    val symbolId: Int,
    val faceUp: Boolean = false,
    val matched: Boolean = false
)

internal fun buildMemoryDeck(pairCount: Int, random: Random = Random.Default): List<MemoryCard> =
    (0 until pairCount)
        .flatMap { listOf(it, it) }
        .shuffled(random)
        .mapIndexed { index, symbolId -> MemoryCard(index = index, symbolId = symbolId) }

internal fun flipUp(deck: List<MemoryCard>, index: Int): List<MemoryCard> =
    deck.map { if (it.index == index) it.copy(faceUp = true) else it }

internal fun isPair(deck: List<MemoryCard>, a: Int, b: Int): Boolean =
    a != b && deck[a].symbolId == deck[b].symbolId

internal fun markMatched(deck: List<MemoryCard>, a: Int, b: Int): List<MemoryCard> =
    deck.map { if (it.index == a || it.index == b) it.copy(matched = true) else it }

internal fun flipDown(deck: List<MemoryCard>, a: Int, b: Int): List<MemoryCard> =
    deck.map { if ((it.index == a || it.index == b) && !it.matched) it.copy(faceUp = false) else it }

internal fun allMatched(deck: List<MemoryCard>): Boolean = deck.all { it.matched }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val cardFaces = listOf("🍒", "🌟", "🎈", "🐢", "🌙", "🍀", "⚓", "🎲", "🦊", "🌵", "🎧", "🍕")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryMatchScreen(
    difficulty: MemoryMatchDifficulty = MemoryMatchDifficulty.Normal,
    onBack: () -> Unit,
    onWin: (moves: Int) -> Unit = {}
) {
    var deck by remember(difficulty) { mutableStateOf(buildMemoryDeck(difficulty.pairCount)) }
    var firstPick by remember(difficulty) { mutableStateOf<Int?>(null) }
    var pendingMismatch by remember(difficulty) { mutableStateOf<Pair<Int, Int>?>(null) }
    var moves by remember(difficulty) { mutableIntStateOf(0) }
    var won by remember(difficulty) { mutableStateOf(false) }

    LaunchedEffect(pendingMismatch) {
        val pair = pendingMismatch ?: return@LaunchedEffect
        delay(700)
        deck = flipDown(deck, pair.first, pair.second)
        pendingMismatch = null
    }

    fun reset() {
        deck = buildMemoryDeck(difficulty.pairCount)
        firstPick = null
        pendingMismatch = null
        moves = 0
        won = false
    }

    fun tap(index: Int) {
        if (won || pendingMismatch != null) return
        val card = deck[index]
        if (card.faceUp || card.matched) return
        deck = flipUp(deck, index)
        val first = firstPick
        if (first == null) {
            firstPick = index
        } else {
            firstPick = null
            moves++
            if (isPair(deck, first, index)) {
                deck = markMatched(deck, first, index)
                if (allMatched(deck)) {
                    won = true
                    onWin(moves)
                }
            } else {
                pendingMismatch = first to index
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Match • ${difficulty.label}") },
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
                if (won) Text("All pairs found!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("New Game") }
            }

            Spacer(Modifier.height(16.dp))

            val cellSize = if (difficulty == MemoryMatchDifficulty.Hard) 52.dp else 64.dp
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (r in 0 until difficulty.rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (c in 0 until difficulty.cols) {
                            val index = r * difficulty.cols + c
                            val card = deck[index]
                            val bg = when {
                                card.matched -> MaterialTheme.colorScheme.surfaceVariant
                                card.faceUp -> Color.White
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .background(bg, RoundedCornerShape(10.dp))
                                    .clickable { tap(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (card.faceUp || card.matched) {
                                    Text(
                                        cardFaces[card.symbolId % cardFaces.size],
                                        fontSize = if (difficulty == MemoryMatchDifficulty.Hard) 22.sp else 28.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tap two cards to flip them. Matching pairs stay face up. Fewer moves = better score.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
