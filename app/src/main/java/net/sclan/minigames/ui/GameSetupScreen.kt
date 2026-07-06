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
    var memoryDifficulty by remember { mutableStateOf(MemoryMatchDifficulty.Normal) }
    var reactionMode by remember { mutableStateOf(ReactionTapMode.Standard) }
    var snakeDifficulty by remember { mutableStateOf(SnakeDifficulty.Normal) }
    var fourMode by remember { mutableStateOf(FourInARowMode.TwoPlayer) }
    var dotsSize by remember { mutableStateOf(DotsAndBoxesSize.Small) }
    var wordDifficulty by remember { mutableStateOf(WordSearchDifficulty.Normal) }
    var codeDifficulty by remember { mutableStateOf(CodeBreakerDifficulty.Easy) }
    var sudokuDifficulty by remember { mutableStateOf(SudokuDifficulty.Mini) }
    var bubbleSize by remember { mutableStateOf(BubbleWrapSize.Standard) }
    var stackSpeed by remember { mutableStateOf(TimingStackSpeed.Normal) }
    var mazeSize by remember { mutableStateOf(MazeSize.Medium) }
    var anagramLength by remember { mutableStateOf(AnagramLength.Mixed) }
    var mancalaMode by remember { mutableStateOf(MancalaMode.TwoPlayer) }
    var simonSpeed by remember { mutableStateOf(SimonSpeed.Normal) }
    var waterDifficulty by remember { mutableStateOf(WaterSortDifficulty.Normal) }
    var nutsDifficulty by remember { mutableStateOf(WaterSortDifficulty.Normal) }
    var fillDifficulty by remember { mutableStateOf(ColorFillDifficulty.Normal) }
    var blocksDifficulty by remember { mutableStateOf(ColorBlocksDifficulty.Normal) }
    var escapePack by remember { mutableStateOf(EscapePack.Rookie) }
    var paintSize by remember { mutableStateOf(MazeSize.Medium) }
    var flappyDifficulty by remember { mutableStateOf(FlappyDifficulty.Normal) }
    var sandBrush by remember { mutableStateOf(SandBrush.Normal) }
    var blockFillMode by remember { mutableStateOf(BlockFillMode.Classic) }
    var mergeChainMode by remember { mutableStateOf(MergeChainMode.Classic) }
    var crossDifficulty by remember { mutableStateOf(CrossMathDifficulty.Normal) }
    var connectDifficulty by remember { mutableStateOf(NumberConnectDifficulty.Normal) }
    var ludoMode by remember { mutableStateOf(LudoMode.VsBot) }
    var slidingSize by remember { mutableStateOf(SlidingSize.Classic) }
    var pongMode by remember { mutableStateOf(PongMode.VsBot) }
    var airHockeyMode by remember { mutableStateOf(PongMode.VsBot) }

    val choice = GameSetupChoice(
        tileDifficulty, mineDifficulty, ticDifficulty, memoryDifficulty, reactionMode,
        snakeDifficulty, fourMode, dotsSize, wordDifficulty, codeDifficulty, sudokuDifficulty, bubbleSize,
        stackSpeed, mazeSize, anagramLength, mancalaMode, simonSpeed,
        waterDifficulty, nutsDifficulty, fillDifficulty, blocksDifficulty,
        escapePack, paintSize, flappyDifficulty, sandBrush, blockFillMode,
        mergeChainMode, crossDifficulty, connectDifficulty,
        ClassicMode.Classic, ludoMode, slidingSize, pongMode, airHockeyMode
    )
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
                            GameId.MemoryMatch -> DifficultyRow(
                                MemoryMatchDifficulty.entries.map { d -> "${d.label} ${d.rows}×${d.cols}" },
                                memoryDifficulty.ordinal
                            ) { memoryDifficulty = MemoryMatchDifficulty.entries[it] }
                            GameId.ReactionTap -> DifficultyRow(
                                ReactionTapMode.entries.map { it.label },
                                reactionMode.ordinal
                            ) { reactionMode = ReactionTapMode.entries[it] }
                            GameId.Snake -> DifficultyRow(
                                SnakeDifficulty.entries.map { it.label },
                                snakeDifficulty.ordinal
                            ) { snakeDifficulty = SnakeDifficulty.entries[it] }
                            GameId.FourInARow -> DifficultyRow(
                                FourInARowMode.entries.map { it.label },
                                fourMode.ordinal
                            ) { fourMode = FourInARowMode.entries[it] }
                            GameId.DotsAndBoxes -> DifficultyRow(
                                DotsAndBoxesSize.entries.map { it.label },
                                dotsSize.ordinal
                            ) { dotsSize = DotsAndBoxesSize.entries[it] }
                            GameId.WordSearch -> DifficultyRow(
                                WordSearchDifficulty.entries.map { it.label },
                                wordDifficulty.ordinal
                            ) { wordDifficulty = WordSearchDifficulty.entries[it] }
                            GameId.CodeBreaker -> DifficultyRow(
                                CodeBreakerDifficulty.entries.map { it.label },
                                codeDifficulty.ordinal
                            ) { codeDifficulty = CodeBreakerDifficulty.entries[it] }
                            GameId.Sudoku -> DifficultyRow(
                                SudokuDifficulty.entries.map { it.label },
                                sudokuDifficulty.ordinal
                            ) { sudokuDifficulty = SudokuDifficulty.entries[it] }
                            GameId.BubbleWrap -> DifficultyRow(
                                BubbleWrapSize.entries.map { it.label },
                                bubbleSize.ordinal
                            ) { bubbleSize = BubbleWrapSize.entries[it] }
                            GameId.TimingStack -> DifficultyRow(
                                TimingStackSpeed.entries.map { it.label },
                                stackSpeed.ordinal
                            ) { stackSpeed = TimingStackSpeed.entries[it] }
                            GameId.MazeRunner -> DifficultyRow(
                                MazeSize.entries.map { it.label },
                                mazeSize.ordinal
                            ) { mazeSize = MazeSize.entries[it] }
                            GameId.AnagramTiles -> DifficultyRow(
                                AnagramLength.entries.map { it.label },
                                anagramLength.ordinal
                            ) { anagramLength = AnagramLength.entries[it] }
                            GameId.Mancala -> DifficultyRow(
                                MancalaMode.entries.map { it.label },
                                mancalaMode.ordinal
                            ) { mancalaMode = MancalaMode.entries[it] }
                            GameId.SimonSays -> DifficultyRow(
                                SimonSpeed.entries.map { it.label },
                                simonSpeed.ordinal
                            ) { simonSpeed = SimonSpeed.entries[it] }
                            GameId.WaterSort -> DifficultyRow(
                                WaterSortDifficulty.entries.map { it.label },
                                waterDifficulty.ordinal
                            ) { waterDifficulty = WaterSortDifficulty.entries[it] }
                            GameId.NutsAndBolts -> DifficultyRow(
                                WaterSortDifficulty.entries.map { it.label },
                                nutsDifficulty.ordinal
                            ) { nutsDifficulty = WaterSortDifficulty.entries[it] }
                            GameId.ColorFill -> DifficultyRow(
                                ColorFillDifficulty.entries.map { it.label },
                                fillDifficulty.ordinal
                            ) { fillDifficulty = ColorFillDifficulty.entries[it] }
                            GameId.ColorBlocks -> DifficultyRow(
                                ColorBlocksDifficulty.entries.map { it.label },
                                blocksDifficulty.ordinal
                            ) { blocksDifficulty = ColorBlocksDifficulty.entries[it] }
                            GameId.Escape -> DifficultyRow(
                                EscapePack.entries.map { it.label },
                                escapePack.ordinal
                            ) { escapePack = EscapePack.entries[it] }
                            GameId.MazePaint -> DifficultyRow(
                                MazeSize.entries.map { it.label },
                                paintSize.ordinal
                            ) { paintSize = MazeSize.entries[it] }
                            GameId.FlappyJump -> DifficultyRow(
                                FlappyDifficulty.entries.map { it.label },
                                flappyDifficulty.ordinal
                            ) { flappyDifficulty = FlappyDifficulty.entries[it] }
                            GameId.SandFall -> DifficultyRow(
                                SandBrush.entries.map { it.label },
                                sandBrush.ordinal
                            ) { sandBrush = SandBrush.entries[it] }
                            GameId.BlockFill -> DifficultyRow(
                                BlockFillMode.entries.map { it.label },
                                blockFillMode.ordinal
                            ) { blockFillMode = BlockFillMode.entries[it] }
                            GameId.MergeChain -> DifficultyRow(
                                MergeChainMode.entries.map { it.label },
                                mergeChainMode.ordinal
                            ) { mergeChainMode = MergeChainMode.entries[it] }
                            GameId.CrossMath -> DifficultyRow(
                                CrossMathDifficulty.entries.map { it.label },
                                crossDifficulty.ordinal
                            ) { crossDifficulty = CrossMathDifficulty.entries[it] }
                            GameId.NumberConnect -> DifficultyRow(
                                NumberConnectDifficulty.entries.map { it.label },
                                connectDifficulty.ordinal
                            ) { connectDifficulty = NumberConnectDifficulty.entries[it] }
                            GameId.Solitaire, GameId.War, GameId.Blackjack, GameId.Dominoes, GameId.Checkers ->
                                DifficultyRow(ClassicMode.entries.map { it.label }, 0) {}
                            GameId.Ludo -> DifficultyRow(
                                LudoMode.entries.map { it.label },
                                ludoMode.ordinal
                            ) { ludoMode = LudoMode.entries[it] }
                            GameId.SlidingPuzzle -> DifficultyRow(
                                SlidingSize.entries.map { it.label },
                                slidingSize.ordinal
                            ) { slidingSize = SlidingSize.entries[it] }
                            GameId.Pong -> DifficultyRow(
                                PongMode.entries.map { it.label },
                                pongMode.ordinal
                            ) { pongMode = PongMode.entries[it] }
                            GameId.AirHockey -> DifficultyRow(
                                PongMode.entries.map { it.label },
                                airHockeyMode.ordinal
                            ) { airHockeyMode = PongMode.entries[it] }
                            GameId.WordRescue, GameId.WordGuess, GameId.PenaltyKicks,
                            GameId.FidgetSpinner, GameId.ChalkDoodle, GameId.WordLadder,
                            GameId.Pool, GameId.Chess ->
                                DifficultyRow(ClassicMode.entries.map { it.label }, 0) {}
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
            GameId.MemoryMatch -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("?"); IconTile("?") }
            GameId.ReactionTap -> Text("⚡", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.Snake -> Text("〰", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.FourInARow -> Text("●●", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            GameId.DotsAndBoxes -> Text("⊞", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.WordSearch -> Text("W", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            GameId.CodeBreaker -> Text("◐◑", style = MaterialTheme.typography.titleLarge, color = Color.White)
            GameId.Sudoku -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("1"); IconTile("9") }
            GameId.BubbleWrap -> Text("○○", style = MaterialTheme.typography.titleLarge, color = Color.White)
            GameId.TimingStack -> Text("▬", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.MazeRunner -> Text("◱", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.AnagramTiles -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("A"); IconTile("Z") }
            GameId.Mancala -> Text("⚈⚈", style = MaterialTheme.typography.titleLarge, color = Color.White)
            GameId.SimonSays -> Text("◩", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.WaterSort -> Text("🧪", style = MaterialTheme.typography.headlineMedium)
            GameId.NutsAndBolts -> Text("🔩", style = MaterialTheme.typography.headlineMedium)
            GameId.ColorFill -> Text("🎨", style = MaterialTheme.typography.headlineMedium)
            GameId.ColorBlocks -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("◼"); IconTile("◼") }
            GameId.Escape -> Text("🚗", style = MaterialTheme.typography.headlineMedium)
            GameId.MazePaint -> Text("🖌", style = MaterialTheme.typography.headlineMedium)
            GameId.FlappyJump -> Text("🐤", style = MaterialTheme.typography.headlineMedium)
            GameId.SandFall -> Text("⏳", style = MaterialTheme.typography.headlineMedium)
            GameId.BlockFill -> Text("▦", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.MergeChain -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("2"); IconTile("4") }
            GameId.CrossMath -> Text("＋＝", style = MaterialTheme.typography.titleLarge, color = Color.White)
            GameId.NumberConnect -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("1"); IconTile("N") }
            GameId.Solitaire -> Text("♠♥", style = MaterialTheme.typography.titleLarge, color = Color.White)
            GameId.War -> Text("⚔", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.Blackjack -> Text("21", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            GameId.Dominoes -> Text("🁫", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            GameId.Checkers -> Text("⛀⛂", style = MaterialTheme.typography.titleLarge, color = Color.White)
            GameId.Ludo -> Text("🎲", style = MaterialTheme.typography.headlineMedium)
            GameId.WordRescue -> Text("🎈", style = MaterialTheme.typography.headlineMedium)
            GameId.WordGuess -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("W"); IconTile("?") }
            GameId.SlidingPuzzle -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("15"); IconTile("↔") }
            GameId.Pong -> Text("🏓", style = MaterialTheme.typography.headlineMedium)
            GameId.PenaltyKicks -> Text("⚽", style = MaterialTheme.typography.headlineMedium)
            GameId.FidgetSpinner -> Text("🌀", style = MaterialTheme.typography.headlineMedium)
            GameId.ChalkDoodle -> Text("🖍", style = MaterialTheme.typography.headlineMedium)
            GameId.WordLadder -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { IconTile("A"); IconTile("B") }
            GameId.AirHockey -> Text("🏒", style = MaterialTheme.typography.headlineMedium)
            GameId.Pool -> Text("🎱", style = MaterialTheme.typography.headlineMedium)
            GameId.Chess -> Text("♛", style = MaterialTheme.typography.headlineMedium, color = Color.White)
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
        GameId.MemoryMatch -> if (scores.memoryBestMoves > 0)
            "Best: ${ScoreLogic.movesLabel(scores.memoryBestMoves)}" else "No saved score yet."
        GameId.ReactionTap -> if (scores.reactionBestMs > 0)
            "Best average: ${ScoreLogic.reactionLabel(scores.reactionBestMs)}" else "No saved score yet."
        GameId.Snake -> if (scores.snakeBestScore > 0)
            "Best: ${scores.snakeBestScore} food" else "No saved score yet."
        GameId.WordSearch -> if (scores.wordSearchBestSecs > 0)
            "Best: ${ScoreLogic.timeLabel(scores.wordSearchBestSecs)}" else "No saved score yet."
        GameId.CodeBreaker -> if (scores.codeBestGuesses > 0)
            "Best: ${scores.codeBestGuesses} guesses" else "No saved score yet."
        GameId.Sudoku -> if (scores.sudokuWins > 0)
            "Solved: ${scores.sudokuWins}" else "No puzzles solved yet."
        GameId.FourInARow -> "Play a friend or challenge the bot."
        GameId.DotsAndBoxes -> "Local pass-and-play duel."
        GameId.BubbleWrap -> "No score. Just relax."
        GameId.TimingStack -> if (scores.stackBestLayers > 0)
            "Best tower: ${scores.stackBestLayers} layers" else "No saved score yet."
        GameId.MazeRunner -> if (scores.mazeBestSecs > 0)
            "Best escape: ${ScoreLogic.timeLabel(scores.mazeBestSecs)}" else "No saved score yet."
        GameId.AnagramTiles -> if (scores.anagramBestSolved > 0)
            "Best round: ${scores.anagramBestSolved} solved" else "No saved score yet."
        GameId.Mancala -> "Classic seed-sowing duel."
        GameId.SimonSays -> if (scores.simonBestRound > 0)
            "Best: ${scores.simonBestRound} rounds" else "No saved score yet."
        GameId.WaterSort -> if (scores.waterSortBestMoves > 0)
            "Best: ${ScoreLogic.movesLabel(scores.waterSortBestMoves)}" else "No saved score yet."
        GameId.NutsAndBolts -> if (scores.nutsBestMoves > 0)
            "Best: ${ScoreLogic.movesLabel(scores.nutsBestMoves)}" else "No saved score yet."
        GameId.ColorFill -> if (scores.colorFillWins > 0)
            "Boards filled: ${scores.colorFillWins}" else "No boards filled yet."
        GameId.ColorBlocks -> if (scores.blocksBestScore > 0)
            "Best score: ${scores.blocksBestScore}" else "No saved score yet."
        GameId.Escape -> if (scores.escapeLevelsBeaten > 0)
            "Levels beaten: ${scores.escapeLevelsBeaten}" else "No escapes yet."
        GameId.MazePaint -> if (scores.paintBestSwipes > 0)
            "Best: ${scores.paintBestSwipes} swipes" else "No saved score yet."
        GameId.FlappyJump -> if (scores.flappyBestScore > 0)
            "Best: ${scores.flappyBestScore} pipes" else "No saved score yet."
        GameId.SandFall -> "Pure relaxation — nothing tracked."
        GameId.BlockFill -> if (scores.blockFillBestScore > 0)
            "Best score: ${scores.blockFillBestScore}" else "No saved score yet."
        GameId.MergeChain -> if (scores.mergeChainBest > 0)
            "Best score: ${scores.mergeChainBest}" else "No saved score yet."
        GameId.CrossMath -> if (scores.crossMathSolved > 0)
            "Solved: ${scores.crossMathSolved}" else "No puzzles solved yet."
        GameId.NumberConnect -> if (scores.numberConnectWins > 0)
            "Paths traced: ${scores.numberConnectWins}" else "No paths traced yet."
        GameId.Solitaire -> if (scores.solitaireWins > 0)
            "Games won: ${scores.solitaireWins}" else "No wins yet."
        GameId.War -> if (scores.warWins > 0)
            "Decks conquered: ${scores.warWins}" else "No wins yet."
        GameId.Blackjack -> if (scores.blackjackWins > 0)
            "Hands won: ${scores.blackjackWins}" else "No hands won yet."
        GameId.Dominoes -> if (scores.dominoWins > 0)
            "Games won: ${scores.dominoWins}" else "No wins yet."
        GameId.Checkers -> if (scores.checkersWins > 0)
            "Games won: ${scores.checkersWins}" else "No wins yet."
        GameId.Ludo -> if (scores.ludoWins > 0)
            "Races won: ${scores.ludoWins}" else "No races won yet."
        GameId.WordRescue -> if (scores.wordRescueWins > 0)
            "Words rescued: ${scores.wordRescueWins}" else "No rescues yet."
        GameId.WordGuess -> if (scores.wordGuessWins > 0)
            "Words cracked: ${scores.wordGuessWins}" else "No words cracked yet."
        GameId.SlidingPuzzle -> if (scores.slidingBestMoves > 0)
            "Best: ${ScoreLogic.movesLabel(scores.slidingBestMoves)}" else "No saved score yet."
        GameId.Pong -> if (scores.pongWins > 0)
            "Matches won: ${scores.pongWins}" else "No matches won yet."
        GameId.PenaltyKicks -> if (scores.penaltyBestGoals > 0)
            "Best round: ${scores.penaltyBestGoals}/5 goals" else "No rounds shot yet."
        GameId.FidgetSpinner, GameId.ChalkDoodle -> "Pure relaxation — nothing tracked."
        GameId.WordLadder -> if (scores.wordLadderWins > 0)
            "Ladders climbed: ${scores.wordLadderWins}" else "No ladders climbed yet."
        GameId.AirHockey -> if (scores.airHockeyWins > 0)
            "Matches won: ${scores.airHockeyWins}" else "No matches won yet."
        GameId.Pool -> if (scores.poolBestShots > 0)
            "Best clear: ${scores.poolBestShots} shots" else "No tables cleared yet."
        GameId.Chess -> if (scores.chessWins > 0)
            "Games won: ${scores.chessWins}" else "No wins yet."
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
    GameId.MemoryMatch -> listOf(
        "Flip two cards" to "Tap any two face-down cards to flip them over and see their symbols.",
        "Find the pairs" to "If the symbols match, the pair stays face up. If not, both cards flip back — remember where they were!",
        "Beat your best" to "Clear the whole board on ${choice.memoryMatch.label} (${choice.memoryMatch.rows}×${choice.memoryMatch.cols}) in as few moves as possible."
    )
    GameId.ReactionTap -> listOf(
        "Wait for green" to "Tap to arm a round. The panel turns red — hold steady until it flashes green.",
        "Tap fast" to "The instant it turns green, tap! Your reaction time is measured in milliseconds.",
        "Play ${choice.reactionTap.rounds} rounds" to "Your average over ${choice.reactionTap.rounds} rounds is your score. Tapping early restarts that round."
    )
    GameId.Snake -> listOf(
        "Swipe to steer" to "Swipe up, down, left, or right anywhere on the board to change direction. The snake never stops moving.",
        "Eat to grow" to "Grab the orange food to grow longer and score a point. New food appears somewhere else.",
        "Don't crash" to "Hitting a wall or your own body ends the run. ${choice.snake.label} speed keeps things ${if (choice.snake == SnakeDifficulty.Fast) "spicy" else "manageable"}."
    )
    GameId.FourInARow -> listOf(
        "Drop your discs" to "Tap a column to drop a disc — it falls to the lowest empty slot.",
        "Connect four" to "Line up four of your discs in a row: across, down, or diagonally.",
        "Watch the threats" to "Block your opponent's three-in-a-row before they complete it. Smart Bot will punish mistakes!"
    )
    GameId.DotsAndBoxes -> listOf(
        "Draw lines" to "Take turns tapping the gap between two dots to draw a line.",
        "Close boxes" to "Complete the fourth side of a box to claim it — and take another turn immediately.",
        "Count carefully" to "Avoid drawing the third side of a box, or you'll hand it to your opponent. Most boxes wins."
    )
    GameId.WordSearch -> listOf(
        "Scan the grid" to "${choice.wordSearch.wordCount} words are hidden in the grid — across, down, diagonal, even backwards.",
        "Select a word" to "Tap the first letter of a word, then tap its last letter to select the line between them.",
        "Find them all" to "Found words get crossed off the list. Clear the list as fast as you can."
    )
    GameId.CodeBreaker -> listOf(
        "Crack the code" to "A secret code of 4 colors has been set. Colors can repeat!",
        "Read the pegs" to "After each guess: ● = right color in the right spot, ○ = right color, wrong spot.",
        "Use logic" to "You have ${choice.codeBreaker.maxGuesses} guesses with ${choice.codeBreaker.colors} possible colors. Every guess narrows it down."
    )
    GameId.Sudoku -> listOf(
        "Fill the grid" to "Every row, every column, and every box must contain each number exactly once.",
        "Tap and place" to "Tap a cell, then tap a number. Bold numbers are given and can't be changed.",
        "Fix the reds" to "Numbers turn red when they conflict. The puzzle is solved when the grid is full with no reds."
    )
    GameId.BubbleWrap -> listOf(
        "Pop" to "Tap bubbles to pop them. That's it. That's the game.",
        "Fresh sheets" to "Popped every bubble? Grab a new sheet and keep going. We won't judge."
    )
    GameId.TimingStack -> listOf(
        "Watch it slide" to "A block slides back and forth above your tower. Tap anywhere to drop it.",
        "Keep the overlap" to "Only the part that overlaps the layer below survives. Miss completely and the tower falls.",
        "Reach the top" to "Stack 12 layers to build a perfect tower. ${choice.timingStack.label} speed sets the pace."
    )
    GameId.MazeRunner -> listOf(
        "Slide, don't step" to "Swipe a direction and you glide until you hit a wall — plan each slide.",
        "Find the exit" to "Start top-left, reach the gold dot at the bottom-right.",
        "Race the clock" to "Your escape time is saved. ${choice.mazeRunner.label} mazes are freshly generated every game."
    )
    GameId.AnagramTiles -> listOf(
        "Read the tiles" to "Each round scrambles a real word into letter tiles.",
        "Rebuild the word" to "Tap tiles in order to spell your answer. Tap a placed letter to send it back.",
        "Clear the round" to "${choice.anagramTiles.rounds} words per round. Stuck? Reveal or skip — but those don't count as solved."
    )
    GameId.Mancala -> listOf(
        "Sow your seeds" to "Tap one of your six pits to scoop its seeds and drop one in each pit counter-clockwise.",
        "Earn extra turns" to "If your last seed lands in your store (the big pit on your right), you move again.",
        "Capture" to "Last seed in one of your empty pits? You capture it plus everything in the pit across from it.",
        "End game" to "When one side is empty, remaining seeds go to their owner. Most seeds in store wins."
    )
    GameId.SimonSays -> listOf(
        "Watch the pads" to "The four pads flash in a sequence. Memorize the order.",
        "Repeat it" to "Tap the pads in the same order. Get it right and the sequence grows by one.",
        "How far can you go?" to "One mistake ends the run. ${choice.simonSays.label} speed controls how fast the pads flash."
    )
    GameId.WaterSort -> listOf(
        "Pick up a tube" to "Tap a tube to select it, then tap another tube to pour into it.",
        "Match the tops" to "You can only pour onto the same color, or into an empty tube. Whole runs of one color pour together.",
        "Sort them all" to "The puzzle is solved when every tube holds a single color (or nothing). Fewer moves = better score."
    )
    GameId.NutsAndBolts -> listOf(
        "Grab some nuts" to "Tap a bolt to grab its top nuts, then tap another bolt to screw them on.",
        "Stack by color" to "Nuts only land on a matching color or a bare bolt. Matching nuts move together.",
        "Finish the job" to "Every bolt must end up holding one color. Fewer moves = better score."
    )
    GameId.ColorFill -> listOf(
        "Flood from the corner" to "Your territory starts at the top-left square. Tap a color button to flood-fill your territory with it.",
        "Grow every move" to "Picking the color of neighboring squares absorbs them into your territory.",
        "Beat the limit" to "Turn the whole board one color within ${choice.colorFill.moveLimit} moves."
    )
    GameId.ColorBlocks -> listOf(
        "Find groups" to "Tap any group of two or more touching blocks of the same color to clear it.",
        "Think big" to "Score = (blocks − 1)². A 10-block group is worth 81 points — plan your taps to build big groups.",
        "Clear the board" to "Blocks fall and columns slide left as you clear. Empty the whole board for a +100 bonus."
    )
    GameId.Escape -> listOf(
        "Free the red block" to "The red block wants out through the right edge. Everything else is in the way.",
        "Slide the blockers" to "Tap a block to select it, then swipe to slide it along its track — horizontal blocks slide sideways, vertical ones up and down.",
        "Fewer moves, more glory" to "Each pack has ${choice.escape.count} hand-crafted levels. Solve them in as few moves as you can."
    )
    GameId.MazePaint -> listOf(
        "Slide and paint" to "Swipe to glide until you hit a wall. Every square you pass gets painted.",
        "Cover everything" to "The maze is done when every square is painted — dead ends included.",
        "Plan your route" to "Fewer swipes is better. ${choice.mazePaint.label} mazes are generated fresh every game."
    )
    GameId.FlappyJump -> listOf(
        "Tap to flap" to "Gravity never stops. Each tap gives one flap upward.",
        "Thread the gaps" to "Pipes scroll toward you — slip through the openings without touching anything.",
        "Score the pipes" to "Every pipe you pass is a point. ${choice.flappyJump.label} sets the speed and gap size."
    )
    GameId.SandFall -> listOf(
        "Pour" to "Tap or drag anywhere to pour sand. It falls, piles up, and slides down slopes.",
        "Paint with physics" to "The color shifts as you pour. Build dunes, bury the floor, then clear it and start again."
    )
    GameId.BlockFill -> listOf(
        "Place your pieces" to "Tap one of the three pieces, then tap the board square where its top-left corner should go.",
        "Clear lines" to "Complete a full row or column and it clears for bonus points — rows and columns can combo.",
        "Keep space open" to "You lose when none of your remaining pieces fit. The 3×3 square is the usual culprit!"
    )
    GameId.MergeChain -> listOf(
        "Start a chain" to "Tap a tile, then tap an adjacent tile with the same number to begin a chain.",
        "Keep it growing" to "Extend the chain to neighbors with the same value or exactly double it — diagonals count.",
        "Merge!" to "Re-tap the last tile to merge the chain into the next power of two. New tiles rain in from the top."
    )
    GameId.CrossMath -> listOf(
        "Read the grid" to "Two equations run across, two run down, and they share the four mystery squares.",
        "Place digits" to "Tap a slot, then a number tile. Every equation must be true at the same time.",
        "Watch the ops" to "${choice.crossMath.label}. Tap a filled square to take its number back."
    )
    GameId.NumberConnect -> listOf(
        "Find the path" to "A hidden path visits every square exactly once, numbered 1 to ${choice.numberConnect.gridSize * choice.numberConnect.gridSize}.",
        "Walk it" to "Start on 1 and tap adjacent squares to advance. Checkpoint numbers confirm you're on track.",
        "No dead ends" to "Wrong turn? Tap your last square to step back, or restart the path."
    )
    GameId.Solitaire -> listOf(
        "Build down, alternate colors" to "In the columns, stack cards in descending order with alternating red and black.",
        "Foundations go up" to "Send aces up top, then build each suit A → K. All 52 cards up means you win.",
        "Tap to move" to "Tap the deck to draw. Tap a card, then tap it again to auto-move — foundations first. Kings claim empty columns."
    )
    GameId.War -> listOf(
        "Flip for it" to "You and the bot each flip your top card. Aces are high.",
        "Winner takes both" to "The higher card takes both cards to the bottom of its deck. Ties split.",
        "Total war" to "Whoever collects all 52 cards wins the war."
    )
    GameId.Blackjack -> listOf(
        "Get to 21" to "You and the dealer each get two cards — one dealer card stays hidden.",
        "Hit or stand" to "Take cards to improve your total, but bust past 21 and you lose instantly.",
        "Dealer rules" to "The dealer must hit below 17 and stand on 17+. Aces flex between 11 and 1."
    )
    GameId.Dominoes -> listOf(
        "Match the ends" to "Play a tile whose pips match either open end of the chain.",
        "Draw when stuck" to "No playable tile? Draw from the boneyard until you find one, or pass when it's empty.",
        "Go out first" to "First to play every tile in their hand wins the round."
    )
    GameId.Checkers -> listOf(
        "Diagonal moves" to "Men slide one square diagonally forward on the dark squares.",
        "Jumps are mandatory" to "If you can capture, you must — and chains of jumps continue with the same piece.",
        "Crown your kings" to "Reach the far row to crown a king, which moves and jumps in all four directions."
    )
    GameId.Ludo -> listOf(
        "Roll to run" to "Roll a 6 to bring a token out of your yard onto your start square. Rolling a 6 always grants another roll.",
        "Race the loop" to "Tokens run the track clockwise, then climb their colored home column. An exact roll lands them home.",
        "Capture & safety" to "Land on a rival to send them back to their yard — unless they're on a grey star or a start square.",
        "First home wins" to "Get all four tokens home before anyone else. ${choice.ludo.label} mode."
    )
    GameId.WordRescue -> listOf(
        "Guess letters" to "Tap letters to reveal them in the hidden word. Green keys were in the word.",
        "Mind the balloons" to "Every wrong letter pops one of six balloons. Lose them all and the word escapes.",
        "Build a streak" to "Each rescued word extends your streak — one miss too many resets it."
    )
    GameId.WordGuess -> listOf(
        "Six tries" to "Type a 5-letter word and hit GO. You get six attempts.",
        "Read the colors" to "Green: right letter, right spot. Yellow: in the word, wrong spot. Grey: not in the word.",
        "Narrow it down" to "The keyboard remembers what you've learned. Use it to zero in."
    )
    GameId.SlidingPuzzle -> listOf(
        "Slide the tiles" to "Tap any tile next to the gap to slide it in.",
        "Restore the order" to "Arrange the numbers 1, 2, 3… with the gap in the last corner.",
        "Plan ahead" to "Every shuffle is solvable. Fewer moves = better score."
    )
    GameId.Pong -> listOf(
        "Drag to defend" to "Drag anywhere to move your paddle. In 2-player mode, each side drags their own half.",
        "Add spin" to "Hitting with the edge of your paddle angles the ball — and it speeds up with every hit.",
        "First to 5" to "Miss and your opponent scores. First to ${PONG_WIN_SCORE} takes the match."
    )
    GameId.PenaltyKicks -> listOf(
        "Watch the sweep" to "The aim marker sweeps across the goal — left, center, right.",
        "Shoot!" to "Tap Shoot to lock your corner. The keeper picks a zone at the same moment.",
        "Beat the keeper" to "Score when the keeper guesses wrong. Five shots — how many can you sink?"
    )
    GameId.FidgetSpinner -> listOf(
        "Flick it" to "Swipe across the spinner to give it a spin. Faster flicks, faster spins.",
        "That's it" to "Friction does the rest. Watch the spin counter climb."
    )
    GameId.ChalkDoodle -> listOf(
        "Draw" to "Pick a chalk color and doodle on the board with your finger.",
        "Wipe" to "One tap wipes the board clean. No saving, no judging — just scribbles."
    )
    GameId.WordLadder -> listOf(
        "One letter at a time" to "Turn the first word into the second by changing a single letter each step.",
        "Real words only" to "Every rung has to be a valid word from the game's list.",
        "Beat par" to "There's a shortest ladder shown as your target. Match it — or find your own route."
    )
    GameId.AirHockey -> listOf(
        "Drag your mallet" to "Slide your mallet around your half of the table to strike the puck.",
        "Aim for the goal" to "Knock the puck into your opponent's goal at the far end.",
        "First to seven" to "Defend your own goal while you attack. ${choice.airHockey.label} sets the pace."
    )
    GameId.Pool -> listOf(
        "Aim the cue" to "Drag back from the table and release like a slingshot — the white line shows your aim.",
        "Mind the power" to "A longer drag hits harder. Use angles off the cushions to reach tricky balls.",
        "Clear the table" to "Sink every colored ball to win. Potting the cue ball is a scratch — it just respots."
    )
    GameId.Chess -> listOf(
        "You're White" to "You play the white pieces at the bottom and move first. Tap a piece to see its legal moves.",
        "Real rules" to "Full legal moves including castling, en passant, and check. Pawns auto-promote to a queen.",
        "Beat the bot" to "The bot searches a few moves ahead. Deliver checkmate — or hold a draw by stalemate."
    )
}
