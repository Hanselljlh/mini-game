package net.sclan.minigames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.sclan.minigames.billing.BillingRepository
import net.sclan.minigames.data.ScoreRepository
import net.sclan.minigames.ui.BubbleWrapScreen
import net.sclan.minigames.ui.CodeBreakerScreen
import net.sclan.minigames.ui.DotsAndBoxesScreen
import net.sclan.minigames.ui.FourInARowScreen
import net.sclan.minigames.ui.Game2048Screen
import net.sclan.minigames.ui.GameId
import net.sclan.minigames.ui.GameSetupScreen
import net.sclan.minigames.ui.HomeScreen
import net.sclan.minigames.ui.MemoryMatchScreen
import net.sclan.minigames.ui.MinesweeperScreen
import net.sclan.minigames.ui.ReactionTapScreen
import net.sclan.minigames.ui.Screen
import net.sclan.minigames.ui.SettingsScreen
import net.sclan.minigames.ui.SnakeScreen
import net.sclan.minigames.ui.SudokuScreen
import net.sclan.minigames.ui.TicTacToeScreen
import net.sclan.minigames.ui.WordSearchScreen
import net.sclan.minigames.ui.theme.MiniGameHubTheme

class MainActivity : ComponentActivity() {

    private lateinit var scoreRepo: ScoreRepository
    private lateinit var billingRepo: BillingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scoreRepo = ScoreRepository(this)
        billingRepo = BillingRepository(this)
        billingRepo.connect()

        setContent {
            MiniGameHubTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Home) }
                val scores = scoreRepo.scores
                val purchaseState = billingRepo.purchaseState

                when (val current = screen) {
                    Screen.Home -> HomeScreen(
                        onGameSelect = { screen = Screen.GameSetup(it) },
                        scores = scores,
                        favorites = scoreRepo.favorites,
                        recents = scoreRepo.recents,
                        adsEnabled = billingRepo.areAdsEnabled,
                        onToggleFavorite = { scoreRepo.toggleFavorite(it.name) },
                        onSettings = { screen = Screen.Settings }
                    )
                    is Screen.GameSetup -> GameSetupScreen(
                        game = current.game,
                        scores = scores,
                        onBack = { screen = Screen.Home },
                        onPlay = { choice ->
                            scoreRepo.recordPlayed(current.game.name)
                            screen = when (current.game) {
                                GameId.TileMerge -> Screen.Game2048(choice.tileMerge)
                                GameId.Minesweeper -> Screen.Minesweeper(choice.minesweeper)
                                GameId.TicTacToe -> Screen.TicTacToe(choice.ticTacToe)
                                GameId.MemoryMatch -> Screen.MemoryMatch(choice.memoryMatch)
                                GameId.ReactionTap -> Screen.ReactionTap(choice.reactionTap)
                                GameId.Snake -> Screen.Snake(choice.snake)
                                GameId.FourInARow -> Screen.FourInARow(choice.fourInARow)
                                GameId.DotsAndBoxes -> Screen.DotsAndBoxes(choice.dotsAndBoxes)
                                GameId.WordSearch -> Screen.WordSearch(choice.wordSearch)
                                GameId.CodeBreaker -> Screen.CodeBreaker(choice.codeBreaker)
                                GameId.Sudoku -> Screen.Sudoku(choice.sudoku)
                                GameId.BubbleWrap -> Screen.BubbleWrap(choice.bubbleWrap)
                            }
                        }
                    )
                    is Screen.TicTacToe -> TicTacToeScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.TicTacToe) }
                    )
                    is Screen.Game2048 -> Game2048Screen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.TileMerge) },
                        onBestScore = { tile, score -> scoreRepo.tryUpdateBest2048(tile, score) },
                        onWon = { scoreRepo.record2048Win() }
                    )
                    is Screen.Minesweeper -> MinesweeperScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.Minesweeper) },
                        onWin = { timeSecs -> scoreRepo.recordMinesweeperWin(timeSecs) }
                    )
                    is Screen.MemoryMatch -> MemoryMatchScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.MemoryMatch) },
                        onWin = { moves -> scoreRepo.recordMemoryWin(moves) }
                    )
                    is Screen.ReactionTap -> ReactionTapScreen(
                        mode = current.mode,
                        onBack = { screen = Screen.GameSetup(GameId.ReactionTap) },
                        onFinish = { avgMs -> scoreRepo.recordReactionResult(avgMs) }
                    )
                    is Screen.Snake -> SnakeScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.Snake) },
                        onGameOver = { score -> scoreRepo.recordSnakeRun(score) }
                    )
                    is Screen.FourInARow -> FourInARowScreen(
                        mode = current.mode,
                        onBack = { screen = Screen.GameSetup(GameId.FourInARow) },
                        onFinished = { scoreRepo.recordDuelFinished() }
                    )
                    is Screen.DotsAndBoxes -> DotsAndBoxesScreen(
                        size = current.size,
                        onBack = { screen = Screen.GameSetup(GameId.DotsAndBoxes) },
                        onFinished = { _, _ -> scoreRepo.recordDuelFinished() }
                    )
                    is Screen.WordSearch -> WordSearchScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.WordSearch) },
                        onWin = { secs -> scoreRepo.recordWordSearchWin(secs) }
                    )
                    is Screen.CodeBreaker -> CodeBreakerScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.CodeBreaker) },
                        onWin = { guesses -> scoreRepo.recordCodeBreakerWin(guesses) }
                    )
                    is Screen.Sudoku -> SudokuScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.Sudoku) },
                        onWin = { scoreRepo.recordSudokuWin() }
                    )
                    is Screen.BubbleWrap -> BubbleWrapScreen(
                        size = current.size,
                        onBack = { screen = Screen.GameSetup(GameId.BubbleWrap) },
                        onSheetDone = { scoreRepo.recordBubbleSheet() }
                    )
                    Screen.Settings -> SettingsScreen(
                        onBack = { screen = Screen.Home },
                        purchaseState = purchaseState,
                        scores = scores,
                        onRemoveAds = { billingRepo.launchPurchaseFlow(this@MainActivity) },
                        onRestorePurchases = { billingRepo.checkExistingPurchases() },
                        onDeleteData = { scoreRepo.deleteAllData() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingRepo.disconnect()
    }
}
