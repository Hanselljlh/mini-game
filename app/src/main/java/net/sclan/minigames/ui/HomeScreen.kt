package net.sclan.minigames.ui

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.sclan.minigames.data.HighScores
import net.sclan.minigames.data.ScoreLogic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGameSelect: (GameId) -> Unit,
    scores: HighScores = HighScores(),
    favorites: Set<String> = emptySet(),
    recents: List<String> = emptyList(),
    adsEnabled: Boolean = true,
    dailyGame: GameId? = null,
    dailyDone: Boolean = false,
    onToggleFavorite: (GameId) -> Unit = {},
    onSettings: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<GameCategory?>(null) }

    val filtered = GameRegistry.search(query)
        .filter { selectedCategory == null || it.category == selectedCategory }
    val browsing = query.isNotBlank() || selectedCategory != null

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Pocket Arcade Offline",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Puzzles, cards, arcade & duels — no WiFi, no account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Level ${ScoreLogic.levelForXp(scores.totalXp)} • ${scores.totalXp} XP • ${scores.gamesPlayed} games played",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search games") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") }
                )
                GameCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        label = { Text(category.label) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            if (!browsing && dailyGame != null) {
                val dailyMeta = GameRegistry.meta(dailyGame)
                SectionLabel("TODAY'S OFFLINE PICK")
                Spacer(Modifier.height(8.dp))
                Card(
                    onClick = { onGameSelect(dailyGame) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        MiniGameIcon(dailyGame, modifier = Modifier.width(56.dp).height(56.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                dailyGame.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                dailyMeta.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (dailyDone) "Completed today ✓ (+${DailyChallenge.BONUS_XP} XP earned)"
                                else "Complete it today for +${DailyChallenge.BONUS_XP} XP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            if (browsing) {
                if (filtered.isEmpty()) {
                    Text(
                        "No games match. Try another search.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    GameCardList(filtered, scores, favorites, onGameSelect, onToggleFavorite)
                }
            } else {
                val recentMetas = recents
                    .mapNotNull { name -> GameRegistry.games.firstOrNull { it.id.name == name } }
                    .take(3)
                if (recentMetas.isNotEmpty()) {
                    SectionLabel("CONTINUE PLAYING")
                    Spacer(Modifier.height(8.dp))
                    GameCardList(recentMetas, scores, favorites, onGameSelect, onToggleFavorite)
                    Spacer(Modifier.height(20.dp))
                }

                val favoriteMetas = GameRegistry.games.filter { it.id.name in favorites && it.id.name !in recents.take(3) }
                if (favoriteMetas.isNotEmpty()) {
                    SectionLabel("FAVORITES")
                    Spacer(Modifier.height(8.dp))
                    GameCardList(favoriteMetas, scores, favorites, onGameSelect, onToggleFavorite)
                    Spacer(Modifier.height(20.dp))
                }

                GameCategory.entries.forEach { category ->
                    val inCategory = GameRegistry.games.filter { it.category == category }
                    if (inCategory.isNotEmpty()) {
                        SectionLabel(category.label.uppercase())
                        Spacer(Modifier.height(8.dp))
                        GameCardList(inCategory, scores, favorites, onGameSelect, onToggleFavorite)
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }

            SectionLabel("SETTINGS & PRIVACY")
            Spacer(Modifier.height(8.dp))
            Card(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (adsEnabled) "Remove Ads • Privacy • Data" else "Ads Removed ✓ • Privacy • Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "One-time purchase, restore, and delete-local-data controls",
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
private fun GameCardList(
    metas: List<GameMeta>,
    scores: HighScores,
    favorites: Set<String>,
    onGameSelect: (GameId) -> Unit,
    onToggleFavorite: (GameId) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metas.forEach { meta ->
            GameCard(
                meta = meta,
                badge = scoreBadge(meta.id, scores),
                isFavorite = meta.id.name in favorites,
                onClick = { onGameSelect(meta.id) },
                onToggleFavorite = { onToggleFavorite(meta.id) }
            )
        }
    }
}

private fun scoreBadge(id: GameId, scores: HighScores): String? = when (id) {
    GameId.TileMerge -> if (scores.best2048Tile > 0)
        "Best tile: ${ScoreLogic.tileLabel(scores.best2048Tile)}  •  Score: ${scores.best2048Score}" else null
    GameId.Minesweeper -> if (scores.minesweeperWins > 0)
        "Wins: ${scores.minesweeperWins}  •  Best: ${ScoreLogic.timeLabel(scores.minesweeperBestTimeSecs)}" else null
    GameId.MemoryMatch -> if (scores.memoryBestMoves > 0)
        "Best: ${ScoreLogic.movesLabel(scores.memoryBestMoves)}" else null
    GameId.ReactionTap -> if (scores.reactionBestMs > 0)
        "Best avg: ${ScoreLogic.reactionLabel(scores.reactionBestMs)}" else null
    GameId.Snake -> if (scores.snakeBestScore > 0) "Best: ${scores.snakeBestScore} food" else null
    GameId.WordSearch -> if (scores.wordSearchBestSecs > 0) "Best: ${ScoreLogic.timeLabel(scores.wordSearchBestSecs)}" else null
    GameId.CodeBreaker -> if (scores.codeBestGuesses > 0) "Best: ${scores.codeBestGuesses} guesses" else null
    GameId.Sudoku -> if (scores.sudokuWins > 0) "Solved: ${scores.sudokuWins}" else null
    GameId.TimingStack -> if (scores.stackBestLayers > 0) "Best: ${scores.stackBestLayers} layers" else null
    GameId.MazeRunner -> if (scores.mazeBestSecs > 0) "Best: ${ScoreLogic.timeLabel(scores.mazeBestSecs)}" else null
    GameId.AnagramTiles -> if (scores.anagramBestSolved > 0) "Best: ${scores.anagramBestSolved} solved" else null
    GameId.SimonSays -> if (scores.simonBestRound > 0) "Best: ${scores.simonBestRound} rounds" else null
    GameId.WaterSort -> if (scores.waterSortBestMoves > 0) "Best: ${ScoreLogic.movesLabel(scores.waterSortBestMoves)}" else null
    GameId.NutsAndBolts -> if (scores.nutsBestMoves > 0) "Best: ${ScoreLogic.movesLabel(scores.nutsBestMoves)}" else null
    GameId.ColorFill -> if (scores.colorFillWins > 0) "Filled: ${scores.colorFillWins}" else null
    GameId.ColorBlocks -> if (scores.blocksBestScore > 0) "Best: ${scores.blocksBestScore}" else null
    GameId.Escape -> if (scores.escapeLevelsBeaten > 0) "Levels: ${scores.escapeLevelsBeaten}" else null
    GameId.MazePaint -> if (scores.paintBestSwipes > 0) "Best: ${scores.paintBestSwipes} swipes" else null
    GameId.FlappyJump -> if (scores.flappyBestScore > 0) "Best: ${scores.flappyBestScore} pipes" else null
    GameId.BlockFill -> if (scores.blockFillBestScore > 0) "Best: ${scores.blockFillBestScore}" else null
    GameId.MergeChain -> if (scores.mergeChainBest > 0) "Best: ${scores.mergeChainBest}" else null
    GameId.CrossMath -> if (scores.crossMathSolved > 0) "Solved: ${scores.crossMathSolved}" else null
    GameId.NumberConnect -> if (scores.numberConnectWins > 0) "Paths: ${scores.numberConnectWins}" else null
    GameId.TicTacToe, GameId.FourInARow, GameId.DotsAndBoxes, GameId.BubbleWrap, GameId.Mancala, GameId.SandFall -> null
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
private fun GameCard(
    meta: GameMeta,
    badge: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniGameIcon(meta.id, modifier = Modifier.width(56.dp).height(56.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(meta.id.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(meta.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    badge ?: "${meta.players}  •  ~${meta.estTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
