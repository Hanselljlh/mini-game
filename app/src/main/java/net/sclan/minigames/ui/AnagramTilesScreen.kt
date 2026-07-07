package net.sclan.minigames.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
// Pure game logic
// ---------------------------------------------------------------------------

internal fun anagramWordPool(minLen: Int, maxLen: Int): List<String> =
    WORD_POOL.filter { it.length in minLen..maxLen }

/** Scrambles so the result differs from the original (when possible). */
internal fun scrambleWord(word: String, random: Random = Random.Default): String {
    if (word.toSet().size <= 1) return word
    var attempt = word
    var tries = 0
    while (attempt == word && tries < 20) {
        attempt = word.toList().shuffled(random).joinToString("")
        tries++
    }
    return attempt
}

/** Tiles for the current round: scrambled letters with stable ids for tap tracking. */
internal data class LetterTile(val id: Int, val letter: Char)

internal fun tilesFor(scrambled: String): List<LetterTile> =
    scrambled.mapIndexed { i, ch -> LetterTile(i, ch) }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnagramTilesScreen(
    length: AnagramLength = AnagramLength.Mixed,
    onBack: () -> Unit,
    onFinished: (solved: Int) -> Unit = {}
) {
    val pool = remember(length) { anagramWordPool(length.minLen, length.maxLen).shuffled() }
    var round by remember(length) { mutableIntStateOf(0) }
    var solved by remember(length) { mutableIntStateOf(0) }
    var tiles by remember(length) { mutableStateOf(tilesFor(scrambleWord(pool.first()))) }
    var picked by remember(length) { mutableStateOf(listOf<LetterTile>()) }
    var revealed by remember(length) { mutableStateOf(false) }
    var finished by remember(length) { mutableStateOf(false) }
    var reported by remember(length) { mutableStateOf(false) }

    val totalRounds = minOf(length.rounds, pool.size)
    val currentWord = pool[round.coerceAtMost(pool.size - 1)]
    val guess = picked.joinToString("") { it.letter.toString() }
    val correct = guess.equals(currentWord, ignoreCase = true)

    fun nextRound(didSolve: Boolean) {
        if (didSolve) solved++
        if (round + 1 >= totalRounds) {
            finished = true
            if (!reported) {
                reported = true
                onFinished(solved)
            }
        } else {
            round++
            tiles = tilesFor(scrambleWord(pool[round]))
            picked = emptyList()
            revealed = false
        }
    }

    fun reset() {
        round = 0
        solved = 0
        tiles = tilesFor(scrambleWord(pool.first()))
        picked = emptyList()
        revealed = false
        finished = false
        reported = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anagram Tiles • ${length.label.substringBefore(" •")}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Word ${(round + 1).coerceAtMost(totalRounds)}/$totalRounds", style = MaterialTheme.typography.titleMedium)
                Text("Solved: $solved", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = ::reset) { Text("Restart") }
            }

            Spacer(Modifier.height(28.dp))

            if (finished) {
                Text("Round complete!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("You unscrambled $solved of $totalRounds words.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                Button(onClick = ::reset) { Text("Play Again") }
            } else {
                // Answer slots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in currentWord.indices) {
                        val tile = picked.getOrNull(i)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    when {
                                        correct && picked.size == currentWord.length -> Color(0xFF81C784)
                                        tile != null -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (tile != null) picked = picked.filter { it.id != tile.id }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (tile != null) Text(tile.letter.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Letter tiles
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tiles.forEach { tile ->
                        val used = picked.any { it.id == tile.id }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (used) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (!used && picked.size < currentWord.length) picked = picked + tile
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!used) Text(tile.letter.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (correct && picked.size == currentWord.length) {
                    Text("Correct! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { nextRound(true) }) { Text(if (round + 1 >= totalRounds) "Finish" else "Next Word") }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { picked = emptyList() }, enabled = picked.isNotEmpty()) { Text("Clear") }
                        OutlinedButton(onClick = { revealed = true }) { Text("Reveal") }
                        OutlinedButton(onClick = { nextRound(false) }) { Text("Skip") }
                    }
                    if (revealed) {
                        Spacer(Modifier.height(10.dp))
                        Text("Answer: $currentWord (doesn't count as solved)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Tap letters to build the word. Tap a placed letter to return it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
