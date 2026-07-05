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
import net.sclan.minigames.ui.BlockFillScreen
import net.sclan.minigames.ui.ColorBlocksScreen
import net.sclan.minigames.ui.ColorFillScreen
import net.sclan.minigames.ui.EscapeScreen
import net.sclan.minigames.ui.FlappyJumpScreen
import net.sclan.minigames.ui.MazePaintScreen
import net.sclan.minigames.ui.SandFallScreen
import net.sclan.minigames.ui.NutsAndBoltsScreen
import net.sclan.minigames.ui.WaterSortScreen
import net.sclan.minigames.ui.DailyChallenge
import net.sclan.minigames.ui.DotsAndBoxesScreen
import net.sclan.minigames.ui.FourInARowScreen
import net.sclan.minigames.ui.Game2048Screen
import net.sclan.minigames.ui.GameId
import net.sclan.minigames.ui.GameSetupScreen
import net.sclan.minigames.ui.HomeScreen
import net.sclan.minigames.ui.AnagramTilesScreen
import net.sclan.minigames.ui.MancalaScreen
import net.sclan.minigames.ui.MazeRunnerScreen
import net.sclan.minigames.ui.MemoryMatchScreen
import net.sclan.minigames.ui.MinesweeperScreen
import net.sclan.minigames.ui.TimingStackScreen
import net.sclan.minigames.ui.ReactionTapScreen
import net.sclan.minigames.ui.Screen
import net.sclan.minigames.ui.SettingsScreen
import net.sclan.minigames.ui.SimonSaysScreen
import net.sclan.minigames.ui.SnakeScreen
import net.sclan.minigames.ui.SudokuScreen
import net.sclan.minigames.ui.TicTacToeScreen
import net.sclan.minigames.ui.WordSearchScreen
import net.sclan.minigames.ui.theme.MiniGameHubTheme

class MainActivity : ComponentActivity() {

    private lateinit var scoreRepo: ScoreRepository
    private lateinit var billingRepo: BillingRepository

    /** Completing today's picked game awards the daily bonus (once per day). */
    private fun completeIfDaily(game: GameId) {
        val today = DailyChallenge.todayEpochDay()
        if (DailyChallenge.gameForDay(today) == game) {
            scoreRepo.markDailyComplete(today, DailyChallenge.BONUS_XP)
        }
    }

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
                    Screen.Home -> {
                        val today = DailyChallenge.todayEpochDay()
                        HomeScreen(
                            onGameSelect = { screen = Screen.GameSetup(it) },
                            scores = scores,
                            favorites = scoreRepo.favorites,
                            recents = scoreRepo.recents,
                            adsEnabled = billingRepo.areAdsEnabled,
                            dailyGame = DailyChallenge.gameForDay(today),
                            dailyDone = scoreRepo.dailyDoneDay == today,
                            onToggleFavorite = { scoreRepo.toggleFavorite(it.name) },
                            onSettings = { screen = Screen.Settings }
                        )
                    }
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
                                GameId.TimingStack -> Screen.TimingStack(choice.timingStack)
                                GameId.MazeRunner -> Screen.MazeRunner(choice.mazeRunner)
                                GameId.AnagramTiles -> Screen.AnagramTiles(choice.anagramTiles)
                                GameId.Mancala -> Screen.Mancala(choice.mancala)
                                GameId.SimonSays -> Screen.SimonSays(choice.simonSays)
                                GameId.WaterSort -> Screen.WaterSort(choice.waterSort)
                                GameId.NutsAndBolts -> Screen.NutsAndBolts(choice.nutsAndBolts)
                                GameId.ColorFill -> Screen.ColorFill(choice.colorFill)
                                GameId.ColorBlocks -> Screen.ColorBlocks(choice.colorBlocks)
                                GameId.Escape -> Screen.Escape(choice.escape)
                                GameId.MazePaint -> Screen.MazePaint(choice.mazePaint)
                                GameId.FlappyJump -> Screen.FlappyJump(choice.flappyJump)
                                GameId.SandFall -> Screen.SandFall(choice.sandFall)
                                GameId.BlockFill -> Screen.BlockFill
                            }
                        }
                    )
                    is Screen.TicTacToe -> TicTacToeScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.TicTacToe) },
                        onFinished = {
                            scoreRepo.recordDuelFinished()
                            completeIfDaily(GameId.TicTacToe)
                        }
                    )
                    is Screen.Game2048 -> Game2048Screen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.TileMerge) },
                        onBestScore = { tile, score -> scoreRepo.tryUpdateBest2048(tile, score) },
                        onWon = {
                            scoreRepo.record2048Win()
                            completeIfDaily(GameId.TileMerge)
                        }
                    )
                    is Screen.Minesweeper -> MinesweeperScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.Minesweeper) },
                        onWin = { timeSecs ->
                            scoreRepo.recordMinesweeperWin(timeSecs)
                            completeIfDaily(GameId.Minesweeper)
                        }
                    )
                    is Screen.MemoryMatch -> MemoryMatchScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.MemoryMatch) },
                        onWin = { moves ->
                            scoreRepo.recordMemoryWin(moves)
                            completeIfDaily(GameId.MemoryMatch)
                        }
                    )
                    is Screen.ReactionTap -> ReactionTapScreen(
                        mode = current.mode,
                        onBack = { screen = Screen.GameSetup(GameId.ReactionTap) },
                        onFinish = { avgMs ->
                            scoreRepo.recordReactionResult(avgMs)
                            completeIfDaily(GameId.ReactionTap)
                        }
                    )
                    is Screen.Snake -> SnakeScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.Snake) },
                        onGameOver = { score ->
                            scoreRepo.recordSnakeRun(score)
                            if (score > 0) completeIfDaily(GameId.Snake)
                        }
                    )
                    is Screen.FourInARow -> FourInARowScreen(
                        mode = current.mode,
                        onBack = { screen = Screen.GameSetup(GameId.FourInARow) },
                        onFinished = {
                            scoreRepo.recordDuelFinished()
                            completeIfDaily(GameId.FourInARow)
                        }
                    )
                    is Screen.DotsAndBoxes -> DotsAndBoxesScreen(
                        size = current.size,
                        onBack = { screen = Screen.GameSetup(GameId.DotsAndBoxes) },
                        onFinished = { _, _ ->
                            scoreRepo.recordDuelFinished()
                            completeIfDaily(GameId.DotsAndBoxes)
                        }
                    )
                    is Screen.WordSearch -> WordSearchScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.WordSearch) },
                        onWin = { secs ->
                            scoreRepo.recordWordSearchWin(secs)
                            completeIfDaily(GameId.WordSearch)
                        }
                    )
                    is Screen.CodeBreaker -> CodeBreakerScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.CodeBreaker) },
                        onWin = { guesses ->
                            scoreRepo.recordCodeBreakerWin(guesses)
                            completeIfDaily(GameId.CodeBreaker)
                        }
                    )
                    is Screen.Sudoku -> SudokuScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.Sudoku) },
                        onWin = {
                            scoreRepo.recordSudokuWin()
                            completeIfDaily(GameId.Sudoku)
                        }
                    )
                    is Screen.BubbleWrap -> BubbleWrapScreen(
                        size = current.size,
                        onBack = { screen = Screen.GameSetup(GameId.BubbleWrap) },
                        onSheetDone = {
                            scoreRepo.recordBubbleSheet()
                            completeIfDaily(GameId.BubbleWrap)
                        }
                    )
                    is Screen.TimingStack -> TimingStackScreen(
                        speed = current.speed,
                        onBack = { screen = Screen.GameSetup(GameId.TimingStack) },
                        onFinished = { layers ->
                            scoreRepo.recordStackRun(layers)
                            if (layers > 0) completeIfDaily(GameId.TimingStack)
                        }
                    )
                    is Screen.MazeRunner -> MazeRunnerScreen(
                        size = current.size,
                        onBack = { screen = Screen.GameSetup(GameId.MazeRunner) },
                        onWin = { secs ->
                            scoreRepo.recordMazeWin(secs)
                            completeIfDaily(GameId.MazeRunner)
                        }
                    )
                    is Screen.AnagramTiles -> AnagramTilesScreen(
                        length = current.length,
                        onBack = { screen = Screen.GameSetup(GameId.AnagramTiles) },
                        onFinished = { solved ->
                            scoreRepo.recordAnagramRound(solved)
                            if (solved > 0) completeIfDaily(GameId.AnagramTiles)
                        }
                    )
                    is Screen.Mancala -> MancalaScreen(
                        mode = current.mode,
                        onBack = { screen = Screen.GameSetup(GameId.Mancala) },
                        onFinished = { _, _ ->
                            scoreRepo.recordDuelFinished()
                            completeIfDaily(GameId.Mancala)
                        }
                    )
                    is Screen.SimonSays -> SimonSaysScreen(
                        speed = current.speed,
                        onBack = { screen = Screen.GameSetup(GameId.SimonSays) },
                        onFinished = { rounds ->
                            scoreRepo.recordSimonRun(rounds)
                            if (rounds > 0) completeIfDaily(GameId.SimonSays)
                        }
                    )
                    is Screen.WaterSort -> WaterSortScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.WaterSort) },
                        onWin = { moves ->
                            scoreRepo.recordWaterSortWin(moves)
                            completeIfDaily(GameId.WaterSort)
                        }
                    )
                    is Screen.NutsAndBolts -> NutsAndBoltsScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.NutsAndBolts) },
                        onWin = { moves ->
                            scoreRepo.recordNutsWin(moves)
                            completeIfDaily(GameId.NutsAndBolts)
                        }
                    )
                    is Screen.ColorFill -> ColorFillScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.ColorFill) },
                        onWin = {
                            scoreRepo.recordColorFillWin()
                            completeIfDaily(GameId.ColorFill)
                        }
                    )
                    is Screen.ColorBlocks -> ColorBlocksScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.ColorBlocks) },
                        onFinished = { score ->
                            scoreRepo.recordBlocksRun(score)
                            if (score > 0) completeIfDaily(GameId.ColorBlocks)
                        }
                    )
                    is Screen.Escape -> EscapeScreen(
                        pack = current.pack,
                        onBack = { screen = Screen.GameSetup(GameId.Escape) },
                        onLevelDone = { _, _ ->
                            scoreRepo.recordEscapeLevel()
                            completeIfDaily(GameId.Escape)
                        }
                    )
                    is Screen.MazePaint -> MazePaintScreen(
                        size = current.size,
                        onBack = { screen = Screen.GameSetup(GameId.MazePaint) },
                        onWin = { swipes ->
                            scoreRepo.recordMazePaintWin(swipes)
                            completeIfDaily(GameId.MazePaint)
                        }
                    )
                    is Screen.FlappyJump -> FlappyJumpScreen(
                        difficulty = current.difficulty,
                        onBack = { screen = Screen.GameSetup(GameId.FlappyJump) },
                        onGameOver = { score ->
                            scoreRepo.recordFlappyRun(score)
                            if (score > 0) completeIfDaily(GameId.FlappyJump)
                        }
                    )
                    is Screen.SandFall -> SandFallScreen(
                        brush = current.brush,
                        onBack = { screen = Screen.GameSetup(GameId.SandFall) }
                    )
                    Screen.BlockFill -> BlockFillScreen(
                        onBack = { screen = Screen.GameSetup(GameId.BlockFill) },
                        onGameOver = { score ->
                            scoreRepo.recordBlockFillRun(score)
                            if (score > 0) completeIfDaily(GameId.BlockFill)
                        }
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
