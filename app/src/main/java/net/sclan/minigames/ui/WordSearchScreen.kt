package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal val WORD_POOL = listOf(
    "ARCADE", "PUZZLE", "OFFLINE", "TRAVEL", "POCKET", "SNAKE", "TILE", "MERGE",
    "BUBBLE", "MEMORY", "BRAIN", "LOGIC", "SCORE", "LEVEL", "BADGE", "DUEL",
    "PIXEL", "SWIPE", "BOARD", "CARDS", "MINES", "FLAG", "COMBO", "STREAK",
    "PLANE", "SIGNAL", "BATTERY", "PLAYER", "WINNER", "TROPHY", "GAMES", "QUEST"
)

internal data class PlacedWord(val word: String, val cells: List<Pair<Int, Int>>)

internal data class WordSearchPuzzle(val size: Int, val grid: List<List<Char>>, val words: List<PlacedWord>)

private val WS_DIRS = listOf(1 to 0, 0 to 1, 1 to 1, 1 to -1, -1 to 0, 0 to -1, -1 to -1, -1 to 1)

internal fun generateWordSearch(
    size: Int,
    wordCount: Int,
    random: Random = Random.Default
): WordSearchPuzzle {
    val cells = Array(size) { CharArray(size) { ' ' } }
    val placed = mutableListOf<PlacedWord>()
    val candidates = WORD_POOL.filter { it.length <= size }.shuffled(random)

    for (word in candidates) {
        if (placed.size >= wordCount) break
        var done = false
        var attempts = 0
        while (!done && attempts < 200) {
            attempts++
            val (dc, dr) = WS_DIRS[random.nextInt(WS_DIRS.size)]
            val row = random.nextInt(size)
            val col = random.nextInt(size)
            val endRow = row + dr * (word.length - 1)
            val endCol = col + dc * (word.length - 1)
            if (endRow !in 0 until size || endCol !in 0 until size) continue
            val path = word.indices.map { i -> (row + dr * i) to (col + dc * i) }
            val ok = word.indices.all { i ->
                val (r, c) = path[i]
                cells[r][c] == ' ' || cells[r][c] == word[i]
            }
            if (!ok) continue
            word.indices.forEach { i ->
                val (r, c) = path[i]
                cells[r][c] = word[i]
            }
            placed.add(PlacedWord(word, path))
            done = true
        }
    }

    val alphabet = ('A'..'Z').toList()
    val grid = List(size) { r ->
        List(size) { c -> if (cells[r][c] == ' ') alphabet[random.nextInt(alphabet.size)] else cells[r][c] }
    }
    return WordSearchPuzzle(size, grid, placed)
}

/** Cells on a straight line (horizontal, vertical, or 45° diagonal) between two points, or null. */
internal fun lineBetween(start: Pair<Int, Int>, end: Pair<Int, Int>): List<Pair<Int, Int>>? {
    val dr = end.first - start.first
    val dc = end.second - start.second
    val steps = maxOf(kotlin.math.abs(dr), kotlin.math.abs(dc))
    if (steps == 0) return listOf(start)
    val validLine = dr == 0 || dc == 0 || kotlin.math.abs(dr) == kotlin.math.abs(dc)
    if (!validLine) return null
    val stepR = Integer.signum(dr)
    val stepC = Integer.signum(dc)
    return (0..steps).map { i -> (start.first + stepR * i) to (start.second + stepC * i) }
}

/** Which placed word (not yet found) matches this cell path, in either direction? */
internal fun matchWord(puzzle: WordSearchPuzzle, path: List<Pair<Int, Int>>, found: Set<String>): PlacedWord? =
    puzzle.words.firstOrNull { pw ->
        pw.word !in found && (pw.cells == path || pw.cells == path.reversed())
    }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordSearchScreen(
    difficulty: WordSearchDifficulty = WordSearchDifficulty.Normal,
    onBack: () -> Unit,
    onWin: (timeSecs: Long) -> Unit = {}
) {
    var puzzle by remember(difficulty) { mutableStateOf(generateWordSearch(difficulty.gridSize, difficulty.wordCount)) }
    var found by remember(difficulty) { mutableStateOf(setOf<String>()) }
    var foundCells by remember(difficulty) { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var selStart by remember(difficulty) { mutableStateOf<Pair<Int, Int>?>(null) }
    var startTime by remember(difficulty) { mutableLongStateOf(0L) }
    var won by remember(difficulty) { mutableStateOf(false) }

    fun reset() {
        puzzle = generateWordSearch(difficulty.gridSize, difficulty.wordCount)
        found = emptySet()
        foundCells = emptySet()
        selStart = null
        startTime = 0L
        won = false
    }

    fun tap(cell: Pair<Int, Int>) {
        if (won) return
        if (startTime == 0L) startTime = System.currentTimeMillis()
        val start = selStart
        if (start == null) {
            selStart = cell
            return
        }
        if (start == cell) {
            selStart = null
            return
        }
        val path = lineBetween(start, cell)
        selStart = null
        if (path != null) {
            val match = matchWord(puzzle, path, found)
            if (match != null) {
                found = found + match.word
                foundCells = foundCells + match.cells
                if (found.size == puzzle.words.size) {
                    won = true
                    val secs = (System.currentTimeMillis() - startTime) / 1000L
                    onWin(secs)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Word Search • ${difficulty.label}") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Found ${found.size}/${puzzle.words.size}", style = MaterialTheme.typography.titleMedium)
                if (won) Text("Solved!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("New Puzzle") }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    for (r in 0 until puzzle.size) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until puzzle.size) {
                                val cell = r to c
                                val bg = when {
                                    cell == selStart -> MaterialTheme.colorScheme.tertiary
                                    cell in foundCells -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.dp)
                                        .background(bg, RoundedCornerShape(4.dp))
                                        .clickable { tap(cell) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        puzzle.grid[r][c].toString(),
                                        fontSize = if (puzzle.size > 10) 11.sp else 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (cell == selStart) MaterialTheme.colorScheme.onTertiary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Tap the first letter of a word, then its last letter.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                puzzle.words.chunked(3).forEach { rowWords ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowWords.forEach { pw ->
                            Text(
                                pw.word,
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (pw.word in found) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (pw.word in found) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
