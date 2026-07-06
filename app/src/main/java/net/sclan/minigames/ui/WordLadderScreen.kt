package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — Word Ladder over a curated 4-letter dictionary.
// ---------------------------------------------------------------------------

internal val LADDER_WORDS: Set<String> = setOf(
    "COLD", "CORD", "CARD", "WARD", "WARM", "WORM", "WORD", "WORE", "WARE", "CARE",
    "CORE", "BORE", "BONE", "BANE", "CANE", "LANE", "LINE", "LIME", "TIME", "TILE",
    "TALE", "TALL", "BALL", "BELL", "BELT", "BOLT", "BOAT", "COAT", "GOAT", "GOAD",
    "LOAD", "ROAD", "READ", "BEAD", "BEAT", "HEAT", "HEAD", "HEAL", "HEEL", "REEL",
    "PEEL", "PEAL", "PEAR", "BEAR", "BEAN", "BEEN", "TEEN", "TEAL", "TELL", "TAIL",
    "FAIL", "FALL", "FULL", "BULL", "BUSK", "BUSH", "GUSH", "GASH", "CASH", "CAST",
    "COST", "COAT", "MOAT", "MEAT", "MEAL", "SEAL", "SEAT", "SEAR", "STAR", "SCAR",
    "SCAT", "SLAT", "SLOT", "SHOT", "SHOP", "STOP", "STEP", "STEM", "STEW", "SLEW",
    "FLEW", "FLED", "FRED", "FREE", "TREE", "TREK", "TRIP", "GRIP", "GRIM", "GRID",
    "GRAD", "GLAD", "GLEN", "GLEE", "FLEE", "FLAG", "FLAT", "FEAT", "FEET", "MEET",
    "MELT", "MOLT", "BOLT", "BOOT", "BOON", "MOON", "MOOD", "GOOD", "GOLD", "HOLD",
    "HELD", "HERD", "HARD", "HART", "PART", "PORT", "SORT", "SOFT", "LOFT", "LIFT",
    "GIFT", "GILT", "GIRT", "DIRT", "DART", "DARK", "PARK", "PACK", "PICK", "PINK",
    "PING", "KING", "RING", "RANG", "RANK", "SANK", "SINK", "SILK", "SILT", "SALT"
)

internal fun ladderNeighbors(word: String, dict: Set<String>): List<String> =
    buildList {
        for (i in word.indices) {
            for (ch in 'A'..'Z') {
                if (ch == word[i]) continue
                val candidate = word.substring(0, i) + ch + word.substring(i + 1)
                if (candidate in dict) add(candidate)
            }
        }
    }

/** Exactly one differing position? */
internal fun oneLetterApart(a: String, b: String): Boolean =
    a.length == b.length && a.indices.count { a[it] != b[it] } == 1

/** BFS shortest ladder from [start] to [end] inclusive, or null if unreachable. */
internal fun findLadder(start: String, end: String, dict: Set<String>): List<String>? {
    if (start == end) return listOf(start)
    val queue = ArrayDeque(listOf(start))
    val prev = mutableMapOf(start to start)
    while (queue.isNotEmpty()) {
        val word = queue.removeFirst()
        for (next in ladderNeighbors(word, dict)) {
            if (next !in prev) {
                prev[next] = word
                if (next == end) {
                    val path = ArrayDeque<String>()
                    var cur = end
                    while (cur != start) { path.addFirst(cur); cur = prev[cur]!! }
                    path.addFirst(start)
                    return path.toList()
                }
                queue.add(next)
            }
        }
    }
    return null
}

/** Picks a start/end pair with a shortest ladder of length in [minLen]..[maxLen] rungs. */
internal fun generateLadderPuzzle(
    dict: Set<String>,
    minLen: Int = 4,
    maxLen: Int = 6,
    random: Random = Random.Default
): Pair<String, String> {
    val words = dict.toList()
    repeat(400) {
        val start = words[random.nextInt(words.size)]
        // BFS distances from start
        val dist = mutableMapOf(start to 0)
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val w = queue.removeFirst()
            ladderNeighbors(w, dict).forEach { n ->
                if (n !in dist) { dist[n] = dist[w]!! + 1; queue.add(n) }
            }
        }
        val candidates = dist.filter { it.value in (minLen - 1)..(maxLen - 1) }.keys.toList()
        if (candidates.isNotEmpty()) {
            return start to candidates[random.nextInt(candidates.size)]
        }
    }
    // Fallback: the classic ladder
    return "COLD" to "WARM"
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordLadderScreen(
    onBack: () -> Unit,
    onWin: (rungs: Int) -> Unit = {}
) {
    var puzzle by remember { mutableStateOf(generateLadderPuzzle(LADDER_WORDS)) }
    var chain by remember { mutableStateOf(listOf(puzzle.first)) }
    var draft by remember { mutableStateOf(puzzle.first) }
    var selectedPos by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }
    var par by remember { mutableIntStateOf(findLadder(puzzle.first, puzzle.second, LADDER_WORDS)?.size ?: 0) }

    val won = chain.last() == puzzle.second

    fun newPuzzle() {
        puzzle = generateLadderPuzzle(LADDER_WORDS)
        chain = listOf(puzzle.first)
        draft = puzzle.first
        selectedPos = 0
        message = ""
        par = findLadder(puzzle.first, puzzle.second, LADDER_WORDS)?.size ?: 0
    }

    fun setLetter(ch: Char) {
        if (won) return
        draft = draft.substring(0, selectedPos) + ch + draft.substring(selectedPos + 1)
        selectedPos = (selectedPos + 1) % 4
    }

    fun submit() {
        if (won) return
        val current = chain.last()
        when {
            draft == current -> message = "Change one letter first."
            !oneLetterApart(current, draft) -> message = "Only one letter at a time."
            draft !in LADDER_WORDS -> message = "\"$draft\" isn't in the word list."
            else -> {
                chain = chain + draft
                message = ""
                if (draft == puzzle.second) onWin(chain.size)
            }
        }
    }

    fun undo() {
        if (chain.size > 1 && !won) {
            chain = chain.dropLast(1)
            draft = chain.last()
            message = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Word Ladder") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$par-rung target", style = MaterialTheme.typography.titleMedium)
                Text("You: ${chain.size}", fontWeight = FontWeight.SemiBold)
                Button(onClick = ::newPuzzle) { Text("New") }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "${puzzle.first}  →  ${puzzle.second}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(14.dp))

            // Chain so far
            chain.forEach { rung ->
                Text(
                    rung,
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 3.sp,
                    color = if (rung == puzzle.second) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (rung == puzzle.second) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(Modifier.height(14.dp))

            if (won) {
                Text("Solved in ${chain.size} rungs! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Button(onClick = ::newPuzzle) { Text("Next Puzzle") }
            } else {
                // Draft tiles
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    draft.forEachIndexed { i, ch ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (i == selectedPos) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    2.dp,
                                    if (i == selectedPos) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPos = i },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ch.toString(), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                if (message.isNotEmpty()) {
                    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                }

                // Letter grid
                listOf("ABCDEFGHI", "JKLMNOPQR", "STUVWXYZ").forEach { rowLetters ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        rowLetters.forEach { letter ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                    .clickable { setLetter(letter) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(letter.toString(), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = ::undo, enabled = chain.size > 1) { Text("Undo") }
                    Button(onClick = ::submit) { Text("Enter Word") }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Change one letter at a time to turn the first word into the second. Every step must be a real word from the list.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
