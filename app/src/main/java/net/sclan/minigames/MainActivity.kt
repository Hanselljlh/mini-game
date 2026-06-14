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
import net.sclan.minigames.ui.Game2048Screen
import net.sclan.minigames.ui.HomeScreen
import net.sclan.minigames.ui.MinesweeperScreen
import net.sclan.minigames.ui.Screen
import net.sclan.minigames.ui.SettingsScreen
import net.sclan.minigames.ui.TicTacToeScreen
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

                // Both repos expose Compose observable state — reads here
                // auto-trigger recomposition when scores or purchase state change.
                val scores = scoreRepo.scores
                val purchaseState = billingRepo.purchaseState

                when (screen) {
                    Screen.Home -> HomeScreen(
                        onGameSelect = { screen = it },
                        scores = scores,
                        adsEnabled = billingRepo.areAdsEnabled,
                        onSettings = { screen = Screen.Settings }
                    )
                    Screen.TicTacToe -> TicTacToeScreen(onBack = { screen = Screen.Home })
                    Screen.Game2048 -> Game2048Screen(
                        onBack = { screen = Screen.Home },
                        onBestScore = { tile, score -> scoreRepo.tryUpdateBest2048(tile, score) }
                    )
                    Screen.Minesweeper -> MinesweeperScreen(
                        onBack = { screen = Screen.Home },
                        onWin = { timeSecs -> scoreRepo.recordMinesweeperWin(timeSecs) }
                    )
                    Screen.Settings -> SettingsScreen(
                        onBack = { screen = Screen.Home },
                        purchaseState = purchaseState,
                        onRemoveAds = { billingRepo.launchPurchaseFlow(this@MainActivity) },
                        onRestorePurchases = { billingRepo.checkExistingPurchases() }
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
