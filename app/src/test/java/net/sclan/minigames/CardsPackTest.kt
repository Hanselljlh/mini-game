package net.sclan.minigames

import net.sclan.minigames.ui.CardSuit
import net.sclan.minigames.ui.CkMove
import net.sclan.minigames.ui.Domino
import net.sclan.minigames.ui.PlayingCard
import net.sclan.minigames.ui.blackjackValue
import net.sclan.minigames.ui.canGoOnFoundation
import net.sclan.minigames.ui.canGoOnTableau
import net.sclan.minigames.ui.ckAllMoves
import net.sclan.minigames.ui.ckApply
import net.sclan.minigames.ui.dealerShouldHit
import net.sclan.minigames.ui.dominoPlayable
import net.sclan.minigames.ui.dominoSet
import net.sclan.minigames.ui.moveWasteCard
import net.sclan.minigames.ui.newCheckersBoard
import net.sclan.minigames.ui.newDominoes
import net.sclan.minigames.ui.newSolitaire
import net.sclan.minigames.ui.newWar
import net.sclan.minigames.ui.playDomino
import net.sclan.minigames.ui.solitaireDraw
import net.sclan.minigames.ui.standardDeck
import net.sclan.minigames.ui.warBattle
import net.sclan.minigames.ui.warRank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CardsPackTest {

    // --- Deck ---

    @Test fun deckHas52UniqueCards() {
        val deck = standardDeck()
        assertEquals(52, deck.size)
        assertEquals(52, deck.toSet().size)
        CardSuit.entries.forEach { suit -> assertEquals(13, deck.count { it.suit == suit }) }
    }

    // --- Solitaire ---

    @Test fun dealHas28InTableauAnd24InStock() {
        val s = newSolitaire(Random(1))
        assertEquals(24, s.stock.size)
        assertEquals(28, s.tableau.sumOf { it.size })
        s.tableau.forEachIndexed { i, pile -> assertEquals(i + 1, pile.size) }
        assertTrue(s.faceUpCounts.all { it == 1 })
    }

    @Test fun drawMovesCardToWasteAndRecycles() {
        var s = newSolitaire(Random(2))
        s = solitaireDraw(s)
        assertEquals(23, s.stock.size)
        assertEquals(1, s.waste.size)
        repeat(23) { s = solitaireDraw(s) }
        assertEquals(0, s.stock.size)
        assertEquals(24, s.waste.size)
        s = solitaireDraw(s) // recycle
        assertEquals(24, s.stock.size)
        assertEquals(0, s.waste.size)
    }

    @Test fun foundationRules() {
        val ace = PlayingCard(CardSuit.Hearts, 1)
        val two = PlayingCard(CardSuit.Hearts, 2)
        val twoSpades = PlayingCard(CardSuit.Spades, 2)
        assertTrue(canGoOnFoundation(ace, emptyList()))
        assertFalse(canGoOnFoundation(two, emptyList()))
        assertTrue(canGoOnFoundation(two, listOf(ace)))
        assertFalse(canGoOnFoundation(twoSpades, listOf(ace)))
    }

    @Test fun tableauRules() {
        val redQueen = PlayingCard(CardSuit.Hearts, 12)
        val blackKing = PlayingCard(CardSuit.Spades, 13)
        val blackJack = PlayingCard(CardSuit.Clubs, 11)
        assertTrue(canGoOnTableau(blackKing, emptyList()))
        assertFalse(canGoOnTableau(redQueen, emptyList()))
        assertTrue(canGoOnTableau(redQueen, listOf(blackKing)))
        assertTrue(canGoOnTableau(blackJack, listOf(blackKing, redQueen)))
        assertFalse(canGoOnTableau(blackJack, listOf(redQueen, blackKing)))
    }

    @Test fun wasteAceGoesToFoundation() {
        val s = newSolitaire(Random(3)).copy(waste = listOf(PlayingCard(CardSuit.Clubs, 1)))
        val next = moveWasteCard(s)
        assertNotNull(next)
        assertEquals(1, next!!.foundations.sumOf { it.size })
        assertTrue(next.waste.isEmpty())
    }

    // --- War ---

    @Test fun warSplitsDeckEvenly() {
        val s = newWar(Random(1))
        assertEquals(26, s.playerDeck.size)
        assertEquals(26, s.botDeck.size)
    }

    @Test fun warBattleConservesCards() {
        var s = newWar(Random(2))
        repeat(50) {
            if (!s.finished) s = warBattle(s)
            assertEquals(52, s.playerDeck.size + s.botDeck.size)
        }
    }

    @Test fun acesAreHigh() {
        assertEquals(14, warRank(PlayingCard(CardSuit.Spades, 1)))
        assertTrue(warRank(PlayingCard(CardSuit.Spades, 1)) > warRank(PlayingCard(CardSuit.Hearts, 13)))
    }

    // --- Blackjack ---

    @Test fun blackjackAceFlexes() {
        assertEquals(21, blackjackValue(listOf(PlayingCard(CardSuit.Spades, 1), PlayingCard(CardSuit.Hearts, 10))))
        assertEquals(
            12,
            blackjackValue(
                listOf(PlayingCard(CardSuit.Spades, 1), PlayingCard(CardSuit.Hearts, 1), PlayingCard(CardSuit.Clubs, 10))
            )
        )
    }

    @Test fun faceCardsAreTen() {
        assertEquals(20, blackjackValue(listOf(PlayingCard(CardSuit.Spades, 13), PlayingCard(CardSuit.Hearts, 12))))
    }

    @Test fun dealerStandsOn17() {
        assertFalse(dealerShouldHit(listOf(PlayingCard(CardSuit.Spades, 10), PlayingCard(CardSuit.Hearts, 7))))
        assertTrue(dealerShouldHit(listOf(PlayingCard(CardSuit.Spades, 10), PlayingCard(CardSuit.Hearts, 6))))
    }

    // --- Dominoes ---

    @Test fun doubleSixSetHas28Tiles() {
        val set = dominoSet()
        assertEquals(28, set.size)
        assertEquals(28, set.toSet().size)
    }

    @Test fun dealGivesSevenEach() {
        val s = newDominoes(Random(4))
        assertEquals(7, s.playerHand.size)
        assertEquals(7, s.botHand.size)
        assertEquals(14, s.boneyard.size)
    }

    @Test fun chainOrientationStaysConsistent() {
        var s = newDominoes(Random(5))
        val first = s.playerHand.first()
        s = playDomino(s, first, fromPlayer = true, preferLeft = false)!!
        // Play any playable bot tile and verify the chain links up
        val botTile = s.botHand.firstOrNull { dominoPlayable(s, it) }
        if (botTile != null) {
            s = playDomino(s, botTile, fromPlayer = false, preferLeft = false)!!
            s.chain.zipWithNext().forEach { (x, y) -> assertEquals(x.b, y.a) }
        }
    }

    // --- Checkers ---

    @Test fun startingBoardHasTwelveEach() {
        val b = newCheckersBoard()
        assertEquals(12, b.sumOf { row -> row.count { it == 1 } })
        assertEquals(12, b.sumOf { row -> row.count { it == 2 } })
    }

    @Test fun openingSideHasSevenMoves() {
        assertEquals(7, ckAllMoves(newCheckersBoard(), forPlayer = true).size)
    }

    @Test fun captureIsMandatory() {
        // Craft a position where the player has one capture available
        val board = List(8) { r ->
            List(8) { c ->
                when {
                    r == 4 && c == 3 -> 1
                    r == 3 && c == 2 -> 2
                    else -> 0
                }
            }
        }
        val moves = ckAllMoves(board, forPlayer = true)
        assertEquals(1, moves.size)
        assertNotNull(moves.first().captured)
        val after = ckApply(board, moves.first())
        assertEquals(0, after[3][2])
        assertEquals(1, after[2][1])
    }

    @Test fun promotionToKing() {
        val board = List(8) { r ->
            List(8) { c -> if (r == 1 && c == 2) 1 else 0 }
        }
        val move = ckAllMoves(board, forPlayer = true).first()
        val after = ckApply(board, move)
        assertEquals(3, after[move.to.first][move.to.second])
        assertEquals(0, move.to.first)
    }

    @Test fun kingMovesBackwards() {
        val board = List(8) { r ->
            List(8) { c -> if (r == 4 && c == 3) 3 else 0 }
        }
        val moves = ckAllMoves(board, forPlayer = true)
        assertEquals(4, moves.size)
        assertTrue(moves.any { it.to.first > 4 })
    }
}
