package net.sclan.minigames.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onGameSelect: (Screen) -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Mini Game Hub", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text("Play offline, anytime.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(36.dp))
            GameCard("Tic Tac Toe", "Two players — take turns on a 3×3 grid") {
                onGameSelect(Screen.TicTacToe)
            }
            Spacer(Modifier.height(12.dp))
            GameCard("2048", "Swipe to slide and combine tiles") {
                onGameSelect(Screen.Game2048)
            }
            Spacer(Modifier.height(12.dp))
            GameCard("Minesweeper", "Tap to reveal • hold to flag") {
                onGameSelect(Screen.Minesweeper)
            }
        }
    }
}

@Composable
private fun GameCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
