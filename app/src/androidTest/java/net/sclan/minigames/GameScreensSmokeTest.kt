package net.sclan.minigames

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.sclan.minigames.ui.AirHockeyScreen
import net.sclan.minigames.ui.AnagramTilesScreen
import net.sclan.minigames.ui.BlackjackScreen
import net.sclan.minigames.ui.BlockFillScreen
import net.sclan.minigames.ui.BubbleWrapScreen
import net.sclan.minigames.ui.ChalkDoodleScreen
import net.sclan.minigames.ui.CheckersScreen
import net.sclan.minigames.ui.ChessScreen
import net.sclan.minigames.ui.CodeBreakerScreen
import net.sclan.minigames.ui.ColorBlocksScreen
import net.sclan.minigames.ui.ColorFillScreen
import net.sclan.minigames.ui.CrossMathScreen
import net.sclan.minigames.ui.DominoesScreen
import net.sclan.minigames.ui.DotsAndBoxesScreen
import net.sclan.minigames.ui.EscapeScreen
import net.sclan.minigames.ui.FidgetSpinnerScreen
import net.sclan.minigames.ui.FlappyJumpScreen
import net.sclan.minigames.ui.FourInARowScreen
import net.sclan.minigames.ui.Game2048Screen
import net.sclan.minigames.ui.GameId
import net.sclan.minigames.ui.GameSetupScreen
import net.sclan.minigames.ui.HangmanScreen
import net.sclan.minigames.ui.HomeScreen
import net.sclan.minigames.ui.LudoScreen
import net.sclan.minigames.ui.MancalaScreen
import net.sclan.minigames.ui.MazePaintScreen
import net.sclan.minigames.ui.MazeRunnerScreen
import net.sclan.minigames.ui.MemoryMatchScreen
import net.sclan.minigames.ui.MergeChainScreen
import net.sclan.minigames.ui.MinesweeperScreen
import net.sclan.minigames.ui.NumberConnectScreen
import net.sclan.minigames.ui.NutsAndBoltsScreen
import net.sclan.minigames.ui.PenaltyKicksScreen
import net.sclan.minigames.ui.PongScreen
import net.sclan.minigames.ui.PoolScreen
import net.sclan.minigames.ui.ReactionTapScreen
import net.sclan.minigames.ui.SandFallScreen
import net.sclan.minigames.ui.SettingsScreen
import net.sclan.minigames.ui.SimonSaysScreen
import net.sclan.minigames.ui.SlidingPuzzleScreen
import net.sclan.minigames.ui.SnakeScreen
import net.sclan.minigames.ui.SolitaireScreen
import net.sclan.minigames.ui.SudokuScreen
import net.sclan.minigames.ui.TicTacToeScreen
import net.sclan.minigames.ui.TimingStackScreen
import net.sclan.minigames.ui.WarScreen
import net.sclan.minigames.ui.WaterSortScreen
import net.sclan.minigames.ui.WordGuessScreen
import net.sclan.minigames.ui.WordLadderScreen
import net.sclan.minigames.ui.WordSearchScreen
import net.sclan.minigames.ui.theme.MiniGameHubTheme
import net.sclan.minigames.billing.PurchaseState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke harness: launches every game screen with default settings, renders
 * the initial frame, then pumps three seconds of frame time so LaunchedEffect
 * game loops run. Any crash in composition, layout, or a game loop fails the
 * test for exactly that screen.
 */
@RunWith(AndroidJUnit4::class)
class GameScreensSmokeTest {

    @get:Rule
    val rule = createComposeRule()

    private fun smoke(content: @Composable () -> Unit) {
        // Manual clock: screens with endless animation loops (sand, spinner)
        // would otherwise never go idle.
        rule.mainClock.autoAdvance = false
        rule.setContent { MiniGameHubTheme { content() } }
        rule.onRoot().assertExists()
        rule.mainClock.advanceTimeBy(1_000)
        rule.onRoot().assertExists()
        rule.mainClock.advanceTimeBy(2_000)
        rule.onRoot().assertExists()
    }

    // --- Shell ---
    @Test fun home() = smoke { HomeScreen(onGameSelect = {}) }
    @Test fun settings() = smoke {
        SettingsScreen(onBack = {}, purchaseState = PurchaseState.NotPurchased, onRemoveAds = {}, onRestorePurchases = {})
    }
    @Test fun setupScreensForEveryGame() {
        // Drive one composition through every game's setup page: exercises all
        // difficulty rows, hero icons, best-score lines, and instruction text.
        rule.mainClock.autoAdvance = false
        val game = androidx.compose.runtime.mutableStateOf(GameId.TileMerge)
        rule.setContent {
            MiniGameHubTheme {
                GameSetupScreen(
                    game = game.value,
                    scores = net.sclan.minigames.data.HighScores(),
                    onBack = {},
                    onPlay = {}
                )
            }
        }
        GameId.entries.forEach { id ->
            game.value = id
            rule.mainClock.advanceTimeBy(200)
            rule.onRoot().assertExists()
        }
    }

    // --- Brain & Logic ---
    @Test fun tileMerge() = smoke { Game2048Screen(onBack = {}) }
    @Test fun minesweeper() = smoke { MinesweeperScreen(onBack = {}) }
    @Test fun codeBreaker() = smoke { CodeBreakerScreen(onBack = {}) }
    @Test fun sudoku() = smoke { SudokuScreen(onBack = {}) }
    @Test fun mazeRunner() = smoke { MazeRunnerScreen(onBack = {}) }
    @Test fun simonSays() = smoke { SimonSaysScreen(onBack = {}) }
    @Test fun escape() = smoke { EscapeScreen(onBack = {}) }
    @Test fun mergeChain() = smoke { MergeChainScreen(onBack = {}) }
    @Test fun crossMath() = smoke { CrossMathScreen(onBack = {}) }
    @Test fun numberConnect() = smoke { NumberConnectScreen(onBack = {}) }

    // --- Sort & Fill ---
    @Test fun waterSort() = smoke { WaterSortScreen(onBack = {}) }
    @Test fun nutsAndBolts() = smoke { NutsAndBoltsScreen(onBack = {}) }
    @Test fun colorFill() = smoke { ColorFillScreen(onBack = {}) }
    @Test fun colorBlocks() = smoke { ColorBlocksScreen(onBack = {}) }
    @Test fun blockFill() = smoke { BlockFillScreen(onBack = {}) }
    @Test fun slidingPuzzle() = smoke { SlidingPuzzleScreen(onBack = {}) }

    // --- Words ---
    @Test fun wordSearch() = smoke { WordSearchScreen(onBack = {}) }
    @Test fun anagramTiles() = smoke { AnagramTilesScreen(onBack = {}) }
    @Test fun wordRescue() = smoke { HangmanScreen(onBack = {}) }
    @Test fun wordGuess() = smoke { WordGuessScreen(onBack = {}) }
    @Test fun wordLadder() = smoke { WordLadderScreen(onBack = {}) }

    // --- Cards & Classics ---
    @Test fun memoryMatch() = smoke { MemoryMatchScreen(onBack = {}) }
    @Test fun solitaire() = smoke { SolitaireScreen(onBack = {}) }
    @Test fun war() = smoke { WarScreen(onBack = {}) }
    @Test fun blackjack() = smoke { BlackjackScreen(onBack = {}) }
    @Test fun dominoes() = smoke { DominoesScreen(onBack = {}) }

    // --- Arcade ---
    @Test fun reactionTap() = smoke { ReactionTapScreen(onBack = {}) }
    @Test fun snake() = smoke { SnakeScreen(onBack = {}) }
    @Test fun timingStack() = smoke { TimingStackScreen(onBack = {}) }
    @Test fun mazePaint() = smoke { MazePaintScreen(onBack = {}) }
    @Test fun flappyJump() = smoke { FlappyJumpScreen(onBack = {}) }
    @Test fun penaltyKicks() = smoke { PenaltyKicksScreen(onBack = {}) }
    @Test fun pool() = smoke { PoolScreen(onBack = {}) }

    // --- Local Duel ---
    @Test fun ticTacToe() = smoke { TicTacToeScreen(onBack = {}) }
    @Test fun fourInARow() = smoke { FourInARowScreen(onBack = {}) }
    @Test fun dotsAndBoxes() = smoke { DotsAndBoxesScreen(onBack = {}) }
    @Test fun mancala() = smoke { MancalaScreen(onBack = {}) }
    @Test fun checkers() = smoke { CheckersScreen(onBack = {}) }
    @Test fun ludo() = smoke { LudoScreen(onBack = {}) }
    @Test fun pong() = smoke { PongScreen(onBack = {}) }
    @Test fun airHockey() = smoke { AirHockeyScreen(onBack = {}) }
    @Test fun chess() = smoke { ChessScreen(onBack = {}) }

    // --- Relax Toys ---
    @Test fun bubbleWrap() = smoke { BubbleWrapScreen(onBack = {}) }
    @Test fun sandFall() = smoke { SandFallScreen(onBack = {}) }
    @Test fun fidgetSpinner() = smoke { FidgetSpinnerScreen(onBack = {}) }
    @Test fun chalkDoodle() = smoke { ChalkDoodleScreen(onBack = {}) }
}
