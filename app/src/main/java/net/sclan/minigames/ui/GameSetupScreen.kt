package net.sclan.minigames.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.sclan.minigames.data.HighScores
import net.sclan.minigames.data.ScoreLogic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    game: GameId,
    scores: HighScores,
    onBack: () -> Unit,
    onPlay: (GameSetupChoice) -> Unit
) {
    var showInstructions by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(0) }
    var tileDifficulty by remember { mutableStateOf(TileMergeDifficulty.Normal) }
    var mineDifficulty by remember { mutableStateOf(MinesweeperDifficulty.Normal) }
    var ticDifficulty by remember { mutableStateOf(TicTacToeDifficulty.TwoPlayer) }

    val choice = GameSetupChoice(tileDifficulty, mineDifficulty, ticDifficulty)
    val pages = instructionPages(game, choice)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(game.title) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GameHero(game)
            Text(game.shortDescription, style = MaterialTheme.typography.bodyLarge)
            BestScoreLine(game, scores)

            if (showInstructions) {
                InstructionPager(
                    pageTitle = pages[page].first,
                    body = pages[page].second,
                    page = page,
                    pageCount = pages.size,
                    showSwipeCue = game == GameId.TileMerge && page == 0,
                    onPrev = { if (page > 0) page-- },
                    onNext = { if (page < pages.lastIndex) page++ else showInstructions = false }
                )
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Difficulty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        when (game) {
                            GameId.TileMerge -> DifficultyRow(
                                TileMergeDifficulty.entries.map { it.label },
                                tileDifficulty.ordinal
                            ) { tileDifficulty = TileMergeDifficulty.entries[it] }
                            GameId.Minesweeper -> DifficultyRow(
                                MinesweeperDifficulty.entries.map { d -> "${d.label} ${d.config.rows}×${d.config.cols}" },
                                mineDifficulty.ordinal
                            ) { mineDifficulty = MinesweeperDifficulty.entries[it] }
                            GameId.TicTacToe -> DifficultyRow(
                                TicTacToeDifficulty.entries.map { it.label },
                                ticDifficulty.ordinal
                            ) { ticDifficulty = TicTacToeDifficulty.entries[it] }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { showInstructions = true; page = 0 }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                        Text(" Instructions")
                    }
                    Button(onClick = { onPlay(choice) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text(" Play")
                    }
                }
            }
        }
    }
}

@Composable
private fun GameHero(game: GameId) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MiniGameIcon(game)
            Column {
                Text(game.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Offline • quick play", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun MiniGameIcon(game: GameId, modifier: Modifier = Modifier.size(56.dp)) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)).padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (game) {
            GameId.TileMerge -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("2"); IconTile("4") }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("8"); IconTile("+") }
            }
            GameId.Minesweeper -> Text("✦", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.TicTacToe -> Text("X O", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IconTile(text: String) {
    Box(
        Modifier.size(18.dp).background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) { Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
}

@Composable
private fun DifficultyRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            if (isSelected) Button(onClick = { onSelect(index) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
            else OutlinedButton(onClick = { onSelect(index) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
    }
}

@Composable
private fun BestScoreLine(game: GameId, scores: HighScores) {
    val text = when (game) {
        GameId.TileMerge -> if (scores.best2048Tile > 0)
            "Best tile: ${ScoreLogic.tileLabel(scores.best2048Tile)} • Score: ${scores.best2048Score}" else "No saved score yet."
        GameId.Minesweeper -> if (scores.minesweeperWins > 0)
            "Wins: ${scores.minesweeperWins} • Best: ${ScoreLogic.timeLabel(scores.minesweeperBestTimeSecs)}" else "No wins yet."
        GameId.TicTacToe -> "Practice against a friend or bot."
    }
    Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
}

@Composable
private fun InstructionPager(
    pageTitle: String,
    body: String,
    page: Int,
    pageCount: Int,
    showSwipeCue: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("HOW TO PLAY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Text(pageTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyLarge)
            if (showSwipeCue) SwipeFingerCue()
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                repeat(pageCount) { dot ->
                    Box(
                        Modifier.padding(3.dp).size(if (dot == page) 10.dp else 7.dp)
                            .background(if (dot == page) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onPrev, enabled = page > 0) { Text("Back") }
                Button(onClick = onNext) { Text(if (page == pageCount - 1) "OK" else "Next") }
            }
        }
    }
}

@Composable
private fun SwipeFingerCue() {
    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        val y = size.height * 0.45f
        val start = Offset(size.width * 0.25f, y)
        val end = Offset(size.width * 0.75f, y)
        drawLine(Color(0xFF5E35B1), start, end, strokeWidth = 8f, cap = StrokeCap.Round)
        drawCircle(Color(0xFF5E35B1), radius = 16f, center = end)
        drawCircle(Color.White, radius = 24f, center = start, style = Stroke(width = 6f))
        drawCircle(Color.White, radius = 12f, center = start)
    }
}

private fun instructionPages(game: GameId, choice: GameSetupChoice): List<Pair<String, String>> = when (game) {
    GameId.TileMerge -> listOf(
        "Swipe the board" to "Swipe up, down, left, or right. Every tile slides in that direction until it hits the edge or another tile.",
        "Merge matching tiles" to "When two tiles with the same number collide, they combine into one tile with double the value.",
        "Reach the target" to "On ${choice.tileMerge.label}, try to reach ${choice.tileMerge.targetTile}. Keep merging without filling the board.",
        "Avoid a full board" to "If the board fills and no neighboring tiles match, the game ends. Plan your swipes before the board gets crowded."
    )
    GameId.Minesweeper -> listOf(
        "Reveal safe squares" to "Tap a square to reveal it. Numbers tell you how many mines are touching that square.",
        "Flag mines" to "Long-press a square to place or remove a flag. Use flags to mark where you think mines are hiding.",
        "Clear the board" to "Win by revealing every safe square without tapping a mine. ${choice.minesweeper.label} uses ${choice.minesweeper.config.mines} mines."
    )
    GameId.TicTacToe -> listOf(
        "Take turns" to "Place X marks and try to make three in a row across, down, or diagonally.",
        "Choose opponent" to "2 Players is local pass-and-play. Easy Bot makes simple moves. Smart Bot tries to win or block you.",
        "Block threats" to "If your opponent has two in a row, block the third square before they win."
    )
}
