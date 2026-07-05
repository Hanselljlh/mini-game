package net.sclan.minigames

import net.sclan.minigames.ui.MemoryMatchDifficulty
import net.sclan.minigames.ui.allMatched
import net.sclan.minigames.ui.buildMemoryDeck
import net.sclan.minigames.ui.flipDown
import net.sclan.minigames.ui.flipUp
import net.sclan.minigames.ui.isPair
import net.sclan.minigames.ui.markMatched
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MemoryMatchTest {

    @Test fun deckHasTwoCardsPerSymbol() {
        val deck = buildMemoryDeck(pairCount = 8)
        assertEquals(16, deck.size)
        deck.groupBy { it.symbolId }.forEach { (_, cards) ->
            assertEquals(2, cards.size)
        }
    }

    @Test fun deckIndicesMatchPositions() {
        val deck = buildMemoryDeck(pairCount = 6)
        deck.forEachIndexed { position, card -> assertEquals(position, card.index) }
    }

    @Test fun allDifficultiesProduceEvenCardCounts() {
        MemoryMatchDifficulty.entries.forEach { d ->
            assertEquals(0, (d.rows * d.cols) % 2)
            assertEquals(d.rows * d.cols, d.pairCount * 2)
        }
    }

    @Test fun deckStartsFaceDownAndUnmatched() {
        val deck = buildMemoryDeck(pairCount = 4)
        assertTrue(deck.none { it.faceUp })
        assertTrue(deck.none { it.matched })
        assertFalse(allMatched(deck))
    }

    @Test fun flipUpFlipsOnlyTargetCard() {
        val deck = buildMemoryDeck(pairCount = 4)
        val flipped = flipUp(deck, 2)
        assertTrue(flipped[2].faceUp)
        assertEquals(1, flipped.count { it.faceUp })
    }

    @Test fun isPairDetectsMatchingSymbols() {
        val deck = buildMemoryDeck(pairCount = 4, random = Random(42))
        val bySymbol = deck.groupBy { it.symbolId }
        val (a, b) = bySymbol.values.first().map { it.index }
        assertTrue(isPair(deck, a, b))
    }

    @Test fun isPairRejectsSameCardTwice() {
        val deck = buildMemoryDeck(pairCount = 4)
        assertFalse(isPair(deck, 3, 3))
    }

    @Test fun markMatchedThenAllMatchedWins() {
        var deck = buildMemoryDeck(pairCount = 3, random = Random(7))
        deck.groupBy { it.symbolId }.values.forEach { cards ->
            deck = markMatched(deck, cards[0].index, cards[1].index)
        }
        assertTrue(allMatched(deck))
    }

    @Test fun flipDownSkipsMatchedCards() {
        var deck = buildMemoryDeck(pairCount = 3, random = Random(7))
        deck = flipUp(flipUp(deck, 0), 1)
        deck = markMatched(deck, 0, 1)
        deck = flipDown(deck, 0, 1)
        // Matched cards stay face up state-wise (matched flag governs rendering)
        assertTrue(deck[0].matched)
        assertTrue(deck[1].matched)
        assertTrue(deck[0].faceUp)
    }

    @Test fun flipDownFlipsUnmatchedCards() {
        var deck = buildMemoryDeck(pairCount = 3)
        deck = flipUp(flipUp(deck, 0), 1)
        deck = flipDown(deck, 0, 1)
        assertFalse(deck[0].faceUp)
        assertFalse(deck[1].faceUp)
    }
}
