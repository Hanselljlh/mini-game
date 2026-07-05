package net.sclan.minigames.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.sclan.minigames.billing.PurchaseState
import net.sclan.minigames.data.HighScores
import net.sclan.minigames.data.ScoreLogic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    purchaseState: PurchaseState,
    scores: HighScores = HighScores(),
    onRemoveAds: () -> Unit,
    onRestorePurchases: () -> Unit,
    onDeleteData: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete local data?") },
            text = { Text("This permanently erases all scores, favorites, XP, and stats stored on this device. Purchases are not affected and can be restored.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteData()
                    showDeleteDialog = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Pocket Arcade+",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            val stateDesc = when (purchaseState) {
                PurchaseState.Purchased -> "Ads removed — thank you for your support!"
                PurchaseState.Pending -> "Purchase pending — waiting for Google Play…"
                else -> "One purchase removes ads forever, across all current and future games. Ads only ever appear between sessions, never during play."
            }
            Text(
                stateDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (purchaseState !is PurchaseState.Purchased) {
                Button(
                    onClick = onRemoveAds,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = purchaseState !is PurchaseState.Pending
                ) {
                    Text("Remove Ads Forever")
                }
            }
            OutlinedButton(
                onClick = onRestorePurchases,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Purchases")
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "YOUR STATS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Level ${ScoreLogic.levelForXp(scores.totalXp)} • ${scores.totalXp} XP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Games played: ${scores.gamesPlayed}", style = MaterialTheme.typography.bodyMedium)
                    Text("Tile Merge best: ${ScoreLogic.tileLabel(scores.best2048Tile)} (score ${scores.best2048Score})", style = MaterialTheme.typography.bodyMedium)
                    Text("Minesweeper: ${scores.minesweeperWins} wins • best ${ScoreLogic.timeLabel(scores.minesweeperBestTimeSecs)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Memory Match best: ${ScoreLogic.movesLabel(scores.memoryBestMoves)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Reaction Tap best avg: ${ScoreLogic.reactionLabel(scores.reactionBestMs)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Snake best: ${if (scores.snakeBestScore > 0) "${scores.snakeBestScore} food" else "—"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Word Search best: ${ScoreLogic.timeLabel(scores.wordSearchBestSecs)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Code Breaker best: ${if (scores.codeBestGuesses > 0) "${scores.codeBestGuesses} guesses" else "—"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Sudoku solved: ${scores.sudokuWins}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "PRIVACY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "No account. No location. Built for offline play.",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Every game works in airplane mode. Scores, favorites, and XP live only on this device. " +
                            "The network is used solely for Google Play purchases when you start one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Local Data")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
