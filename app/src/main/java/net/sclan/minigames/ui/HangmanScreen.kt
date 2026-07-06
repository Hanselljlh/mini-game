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
// Pure game logic — classic letter-guessing with an original "balloon ride"
// presentation: wrong guesses pop balloons; save the word before they're gone.
// ---------------------------------------------------------------------------

internal const val HANGMAN_LIVES = 6

internal val HANGMAN_WORDS = WORD_POOL + listOf(
    "ADVENTURE", "BALLOON", "CHAMPION", "DISCOVERY", "ELEPHANT", "FESTIVAL",
    "GALAXY", "HORIZON", "ISLAND", "JOURNEY", "KINGDOM", "LANTERN",
    "MOUNTAIN", "NOTEBOOK", "OCTOPUS", "PENGUIN", "RAINBOW", "SUNSHINE",
    "TREASURE", "UMBRELLA", "VOLCANO", "WHISPER", "YOGURT", "ZEPPELIN"
)

internal fun hangmanMasked(word: String, guessed: Set<Char>): String =
    word.map { if (it in guessed) it else '_' }.joinToString(" ")

internal fun hangmanWon(word: String, guessed: Set<Char>): Boolean =
    word.all { it in guessed }

internal fun hangmanWrongCount(word: String, guessed: Set<Char>): Int =
    guessed.count { it !in word }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangmanScreen(
    onBack: () -> Unit,
    onRoundEnd: (won: Boolean) -> Unit = {}
) {
    var word by remember { mutableStateOf(HANGMAN_WORDS.random()) }
    var guessed by remember { mutableStateOf(setOf<Char>()) }
    var streak by remember { mutableIntStateOf(0) }
    var reported by remember { mutableStateOf(false) }

    val wrong = hangmanWrongCount(word, guessed)
    val won = hangmanWon(word, guessed)
    val lost = wrong >= HANGMAN_LIVES

    fun newWord() {
        word = HANGMAN_WORDS.random()
        guessed = emptySet()
        reported = false
    }

    fun guess(letter: Char) {
        if (won || lost || letter in guessed) return
        guessed = guessed + letter
        val nowWon = hangmanWon(word, guessed)
        val nowLost = hangmanWrongCount(word, guessed) >= HANGMAN_LIVES
        if ((nowWon || nowLost) && !reported) {
            reported = true
            if (nowWon) streak++ else streak = 0
            onRoundEnd(nowWon)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Word Rescue") },
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
                Text("Streak: $streak", style = MaterialTheme.typography.titleMedium)
                Button(onClick = ::newWord) { Text("New Word") }
            }

            Spacer(Modifier.height(20.dp))

            // Balloons: each wrong guess pops one
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(HANGMAN_LIVES) { i ->
                    Text(
                        if (i < HANGMAN_LIVES - wrong) "🎈" else "💥",
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                hangmanMasked(word, guessed),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(12.dp))

            when {
                won -> Text("Rescued! 🎉 The word was $word.", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                lost -> Text("All balloons popped! It was $word.", color = Color.Red, fontWeight = FontWeight.Bold)
                else -> Text("${HANGMAN_LIVES - wrong} balloons left", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(20.dp))

            // Keyboard
            listOf("ABCDEFG", "HIJKLMN", "OPQRSTU", "VWXYZ").forEach { rowLetters ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowLetters.forEach { letter ->
                        val used = letter in guessed
                        val inWord = used && letter in word
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    when {
                                        inWord -> Color(0xFF81C784)
                                        used -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !used && !won && !lost) { guess(letter) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(letter.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Guess the word one letter at a time. Every miss pops a balloon — save the word before all six are gone!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
