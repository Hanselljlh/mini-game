package net.sclan.minigames.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.sclan.minigames.data.HighScores
import net.sclan.minigames.data.ScoreLogic

@Composable
fun HomeScreen(
    onGameSelect: (Screen) -> Unit,
    scores: HighScores = HighScores(),
    adsEnabled: Boolean = true,
    onSettings: () -> Unit = {}
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                "Pocket Mini Games",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Play offline, anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))

            SectionLabel("GAMES")
            Spacer(Modifier.height(8.dp))

            GameCard(
                title = "2048",
                subtitle = "Swipe to slide and combine tiles",
                badge = if (scores.best2048Tile > 0)
                    "Best tile: ${ScoreLogic.tileLabel(scores.best2048Tile)}  •  Score: ${scores.best2048Score}"
                else null,
                onClick = { onGameSelect(Screen.Game2048) }
            )
            Spacer(Modifier.height(10.dp))
            GameCard(
                title = "Minesweeper",
                subtitle = "Tap to reveal • hold to flag",
                badge = if (scores.minesweeperWins > 0)
                    "Wins: ${scores.minesweeperWins}  •  Best: ${ScoreLogic.timeLabel(scores.minesweeperBestTimeSecs)}"
                else null,
                onClick = { onGameSelect(Screen.Minesweeper) }
            )
            Spacer(Modifier.height(10.dp))
            GameCard(
                title = "Tic Tac Toe",
                subtitle = "Two players — take turns on a 3×3 grid",
                onClick = { onGameSelect(Screen.TicTacToe) }
            )

            Spacer(Modifier.height(28.dp))
            SectionLabel("SETTINGS")
            Spacer(Modifier.height(8.dp))

            Card(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (adsEnabled) "Remove Ads" else "Ads Removed ✓",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (adsEnabled) "Support the app and hide placeholder ads"
                            else "Thank you for your support!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun GameCard(title: String, subtitle: String, badge: String? = null, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (badge != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
