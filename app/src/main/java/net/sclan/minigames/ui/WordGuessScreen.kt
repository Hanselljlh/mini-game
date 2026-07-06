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
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ---------------------------------------------------------------------------
// Pure game logic — 5-letter word guessing with color feedback
// ---------------------------------------------------------------------------

internal const val WG_LENGTH = 5
internal const val WG_MAX_GUESSES = 6

internal val WORDS_5 = listOf(
    "APPLE", "BEACH", "BRAIN", "BREAD", "BRICK", "CANDY", "CHAIR", "CHESS",
    "CLOUD", "CRANE", "DANCE", "DREAM", "DRINK", "EAGLE", "EARTH", "FLAME",
    "FLOOR", "FRUIT", "GHOST", "GRAPE", "GREEN", "HEART", "HONEY", "HOUSE",
    "JUICE", "LEMON", "LIGHT", "MAGIC", "MONEY", "MUSIC", "NIGHT", "OCEAN",
    "PAINT", "PAPER", "PARTY", "PEACH", "PIANO", "PIZZA", "PLANE", "PLANT",
    "QUEEN", "RADIO", "RIVER", "ROBOT", "SHEEP", "SMILE", "SNAKE", "SPACE",
    "SPOON", "STONE", "STORM", "SUGAR", "TABLE", "TIGER", "TRAIN", "WATER",
    "WHALE", "WHEAT", "WORLD", "ZEBRA"
)

internal enum class WgMark { Correct, Present, Absent }

/** Wordle-style scoring with correct handling of duplicate letters. */
internal fun wgScore(secret: String, guess: String): List<WgMark> {
    val marks = MutableList(WG_LENGTH) { WgMark.Absent }
    val remaining = mutableMapOf<Char, Int>()
    for (i in 0 until WG_LENGTH) {
        if (guess[i] == secret[i]) marks[i] = WgMark.Correct
        else remaining[secret[i]] = (remaining[secret[i]] ?: 0) + 1
    }
    for (i in 0 until WG_LENGTH) {
        if (marks[i] == WgMark.Correct) continue
        val count = remaining[guess[i]] ?: 0
        if (count > 0) {
            marks[i] = WgMark.Present
            remaining[guess[i]] = count - 1
        }
    }
    return marks
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private fun markColor(mark: WgMark?): Color = when (mark) {
    WgMark.Correct -> Color(0xFF66BB6A)
    WgMark.Present -> Color(0xFFFFCA28)
    WgMark.Absent -> Color(0xFF9E9E9E)
    null -> Color.Transparent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordGuessScreen(
    onBack: () -> Unit,
    onRoundEnd: (won: Boolean, guesses: Int) -> Unit = { _, _ -> }
) {
    var secret by remember { mutableStateOf(WORDS_5.random()) }
    var rows by remember { mutableStateOf(listOf<Pair<String, List<WgMark>>>()) }
    var current by remember { mutableStateOf("") }
    var streak by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }

    val won = rows.any { it.second.all { m -> m == WgMark.Correct } }

    fun newWord() {
        secret = WORDS_5.random()
        rows = emptyList()
        current = ""
        done = false
        reported = false
    }

    fun submit() {
        if (done || current.length != WG_LENGTH) return
        val marks = wgScore(secret, current)
        rows = rows + (current to marks)
        current = ""
        val nowWon = marks.all { it == WgMark.Correct }
        if (nowWon || rows.size >= WG_MAX_GUESSES) {
            done = true
            if (!reported) {
                reported = true
                if (nowWon) streak++ else streak = 0
                onRoundEnd(nowWon, rows.size)
            }
        }
    }

    fun type(letter: Char) {
        if (!done && current.length < WG_LENGTH) current += letter
    }

    // Best known state per letter for keyboard coloring
    val keyMarks = mutableMapOf<Char, WgMark>()
    rows.forEach { (guess, marks) ->
        guess.forEachIndexed { i, ch ->
            val existing = keyMarks[ch]
            val incoming = marks[i]
            keyMarks[ch] = when {
                existing == WgMark.Correct || incoming == WgMark.Correct -> WgMark.Correct
                existing == WgMark.Present || incoming == WgMark.Present -> WgMark.Present
                else -> WgMark.Absent
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Word Guess") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Streak: $streak", style = MaterialTheme.typography.titleMedium)
                when {
                    won -> Text("Got it! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    done -> Text("It was $secret", color = Color.Red, fontWeight = FontWeight.Bold)
                    else -> {}
                }
                Button(onClick = ::newWord) { Text("New Word") }
            }

            Spacer(Modifier.height(12.dp))

            // Guess grid
            for (r in 0 until WG_MAX_GUESSES) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (c in 0 until WG_LENGTH) {
                        val (letter, mark) = when {
                            r < rows.size -> rows[r].first[c].toString() to rows[r].second[c]
                            r == rows.size && c < current.length -> current[c].toString() to null
                            else -> "" to null
                        }
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(markColor(mark), RoundedCornerShape(6.dp))
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                letter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (mark != null) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.height(5.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Keyboard
            listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEachIndexed { rowIdx, rowLetters ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (rowIdx == 2) {
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(52.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                .clickable { submit() },
                            contentAlignment = Alignment.Center
                        ) { Text("GO", fontWeight = FontWeight.Bold) }
                    }
                    rowLetters.forEach { letter ->
                        val km = keyMarks[letter]
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(31.dp)
                                .background(
                                    if (km != null) markColor(km) else MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { type(letter) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                letter.toString(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (km != null) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (rowIdx == 2) {
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(52.dp)
                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(6.dp))
                                .clickable { if (current.isNotEmpty()) current = current.dropLast(1) },
                            contentAlignment = Alignment.Center
                        ) { Text("⌫", fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Guess the 5-letter word in 6 tries. Green = right spot, yellow = wrong spot, grey = not in the word.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
