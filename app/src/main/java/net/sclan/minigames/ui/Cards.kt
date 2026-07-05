package net.sclan.minigames.ui

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class CardSuit(val symbol: String, val isRed: Boolean) {
    Spades("♠", false), Hearts("♥", true), Diamonds("♦", true), Clubs("♣", false)
}

/** rank 1 = Ace … 13 = King */
data class PlayingCard(val suit: CardSuit, val rank: Int) {
    val label: String
        get() = when (rank) {
            1 -> "A"; 11 -> "J"; 12 -> "Q"; 13 -> "K"; else -> rank.toString()
        }
    val display: String get() = "$label${suit.symbol}"
    val color: Color get() = if (suit.isRed) Color(0xFFC62828) else Color(0xFF212121)
}

fun standardDeck(): List<PlayingCard> =
    CardSuit.entries.flatMap { suit -> (1..13).map { PlayingCard(suit, it) } }

fun shuffledDeck(random: Random = Random.Default): List<PlayingCard> = standardDeck().shuffled(random)
