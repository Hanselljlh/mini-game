package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
// WAR — pure logic
// ---------------------------------------------------------------------------

/** War rank: aces high. */
internal fun warRank(card: PlayingCard): Int = if (card.rank == 1) 14 else card.rank

internal data class WarState(
    val playerDeck: List<PlayingCard>,
    val botDeck: List<PlayingCard>,
    val playerCard: PlayingCard? = null,
    val botCard: PlayingCard? = null,
    val message: String = "Tap Battle!"
) {
    val finished: Boolean get() = playerDeck.isEmpty() || botDeck.isEmpty()
}

internal fun newWar(random: Random = Random.Default): WarState {
    val deck = shuffledDeck(random)
    return WarState(playerDeck = deck.take(26), botDeck = deck.drop(26))
}

/** One battle; ties split the spoils (simplified war). */
internal fun warBattle(s: WarState): WarState {
    if (s.finished) return s
    val p = s.playerDeck.first()
    val b = s.botDeck.first()
    var playerDeck = s.playerDeck.drop(1)
    var botDeck = s.botDeck.drop(1)
    val message: String
    when {
        warRank(p) > warRank(b) -> {
            playerDeck = playerDeck + p + b
            message = "${p.display} beats ${b.display} — you take both!"
        }
        warRank(b) > warRank(p) -> {
            botDeck = botDeck + b + p
            message = "${b.display} beats ${p.display} — bot takes both."
        }
        else -> {
            playerDeck = playerDeck + p
            botDeck = botDeck + b
            message = "Tie! Each keeps their card."
        }
    }
    return WarState(playerDeck, botDeck, p, b, message)
}

// ---------------------------------------------------------------------------
// BLACKJACK — pure logic
// ---------------------------------------------------------------------------

internal fun blackjackValue(hand: List<PlayingCard>): Int {
    var total = hand.sumOf { c -> if (c.rank == 1) 11 else minOf(c.rank, 10) }
    var aces = hand.count { it.rank == 1 }
    while (total > 21 && aces > 0) {
        total -= 10
        aces--
    }
    return total
}

internal fun dealerShouldHit(hand: List<PlayingCard>): Boolean = blackjackValue(hand) < 17

// ---------------------------------------------------------------------------
// WAR screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarScreen(
    onBack: () -> Unit,
    onFinished: (playerWon: Boolean) -> Unit = {}
) {
    var state by remember { mutableStateOf(newWar()) }
    var reported by remember { mutableStateOf(false) }

    fun battle() {
        if (state.finished) return
        state = warBattle(state)
        if (state.finished && !reported) {
            reported = true
            onFinished(state.botDeck.isEmpty())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("War") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Bot: ${state.botDeck.size} cards", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                BigCard(state.playerCard)
                Text("VS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                BigCard(state.botCard)
            }
            Spacer(Modifier.height(12.dp))
            Text(state.message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text("You: ${state.playerDeck.size} cards", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(20.dp))
            if (state.finished) {
                Text(
                    if (state.botDeck.isEmpty()) "You conquered the deck! 🏆" else "The bot took every card!",
                    fontWeight = FontWeight.Bold,
                    color = if (state.botDeck.isEmpty()) Color(0xFF2E7D32) else Color.Red
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = { state = newWar(); reported = false }) { Text("Play Again") }
            } else {
                Button(onClick = ::battle) { Text("Battle!") }
            }
        }
    }
}

@Composable
private fun BigCard(card: PlayingCard?) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(100.dp)
            .background(if (card == null) Color(0xFF37474F) else Color.White, RoundedCornerShape(10.dp))
            .border(2.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (card != null) Text(card.display, color = card.color, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        else Text("✦", color = Color(0xFF90A4AE), fontSize = 22.sp)
    }
}

// ---------------------------------------------------------------------------
// BLACKJACK screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlackjackScreen(
    onBack: () -> Unit,
    onRoundEnd: (playerWon: Boolean) -> Unit = {}
) {
    var deck by remember { mutableStateOf(shuffledDeck()) }
    var player by remember { mutableStateOf(listOf<PlayingCard>()) }
    var dealer by remember { mutableStateOf(listOf<PlayingCard>()) }
    var inRound by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Press Deal to start.") }
    var wins by remember { mutableIntStateOf(0) }
    var losses by remember { mutableIntStateOf(0) }

    fun draw(): PlayingCard {
        if (deck.isEmpty()) deck = shuffledDeck()
        val card = deck.first()
        deck = deck.drop(1)
        return card
    }

    fun settle(playerBust: Boolean) {
        val pv = blackjackValue(player)
        val dv = blackjackValue(dealer)
        val playerWon = !playerBust && (dv > 21 || pv > dv)
        val push = !playerBust && dv <= 21 && pv == dv
        message = when {
            playerBust -> "Bust with $pv — dealer wins."
            dv > 21 -> "Dealer busts with $dv — you win!"
            pv > dv -> "You win $pv vs $dv!"
            push -> "Push — both have $pv."
            else -> "Dealer wins $dv vs $pv."
        }
        if (!push) {
            if (playerWon) wins++ else losses++
            onRoundEnd(playerWon)
        }
        inRound = false
    }

    fun deal() {
        player = listOf(draw(), draw())
        dealer = listOf(draw(), draw())
        inRound = true
        message = "Hit or Stand?"
        if (blackjackValue(player) == 21) {
            message = "Blackjack! 🎉"
            wins++
            onRoundEnd(true)
            inRound = false
        }
    }

    fun hit() {
        player = player + draw()
        if (blackjackValue(player) > 21) settle(playerBust = true)
    }

    fun stand() {
        while (dealerShouldHit(dealer)) dealer = dealer + draw()
        settle(playerBust = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blackjack") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("W $wins — L $losses", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            Text("Dealer${if (!inRound && dealer.isNotEmpty()) " • ${blackjackValue(dealer)}" else ""}", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dealer.forEachIndexed { i, card ->
                    SmallCard(card, faceUp = !inRound || i == 0)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("You${if (player.isNotEmpty()) " • ${blackjackValue(player)}" else ""}", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                player.forEach { card -> SmallCard(card, faceUp = true) }
            }

            Spacer(Modifier.height(20.dp))
            Text(message, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            if (inRound) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = ::hit) { Text("Hit") }
                    OutlinedButton(onClick = ::stand) { Text("Stand") }
                }
            } else {
                Button(onClick = ::deal) { Text("Deal") }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "Closest to 21 without going over wins. Dealer stands on 17. Aces count as 11 or 1. No chips — bragging rights only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SmallCard(card: PlayingCard, faceUp: Boolean) {
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(72.dp)
            .background(if (faceUp) Color.White else Color(0xFF37474F), RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (faceUp) Text(card.display, color = card.color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        else Text("✦", color = Color(0xFF90A4AE))
    }
}
