package net.sclan.minigames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.sclan.minigames.ui.Game2048Screen
import net.sclan.minigames.ui.HomeScreen
import net.sclan.minigames.ui.MinesweeperScreen
import net.sclan.minigames.ui.Screen
import net.sclan.minigames.ui.TicTacToeScreen
import net.sclan.minigames.ui.theme.MiniGameHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniGameHubTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Home) }
                when (screen) {
                    Screen.Home -> HomeScreen(onGameSelect = { screen = it })
                    Screen.TicTacToe -> TicTacToeScreen(onBack = { screen = Screen.Home })
                    Screen.Game2048 -> Game2048Screen(onBack = { screen = Screen.Home })
                    Screen.Minesweeper -> MinesweeperScreen(onBack = { screen = Screen.Home })
                }
            }
        }
    }
}
