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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal const val CODE_LENGTH = 4

internal fun newSecretCode(colors: Int, random: Random = Random.Default): List<Int> =
    List(CODE_LENGTH) { random.nextInt(colors) }

/** Classic Mastermind feedback: exact = right color+position, partial = right color only. */
internal fun codeFeedback(secret: List<Int>, guess: List<Int>): Pair<Int, Int> {
    val exact = secret.indices.count { secret[it] == guess[it] }
    val secretCounts = secret.groupingBy { it }.eachCount()
    val guessCounts = guess.groupingBy { it }.eachCount()
    val totalMatches = secretCounts.entries.sumOf { (color, count) ->
        minOf(count, guessCounts[color] ?: 0)
    }
    return exact to (totalMatches - exact)
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val pegColors = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFDD835),
    Color(0xFF8E24AA), Color(0xFFFB8C00), Color(0xFF00ACC1), Color(0xFF6D4C41)
)

private data class GuessRecord(val guess: List<Int>, val exact: Int, val partial: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBreakerScreen(
    difficulty: CodeBreakerDifficulty = CodeBreakerDifficulty.Easy,
    onBack: () -> Unit,
    onWin: (guesses: Int) -> Unit = {}
) {
    var secret by remember(difficulty) { mutableStateOf(newSecretCode(difficulty.colors)) }
    var current by remember(difficulty) { mutableStateOf(listOf<Int>()) }
    var history by remember(difficulty) { mutableStateOf(listOf<GuessRecord>()) }
    var won by remember(difficulty) { mutableStateOf(false) }
    var lost by remember(difficulty) { mutableStateOf(false) }

    fun reset() {
        secret = newSecretCode(difficulty.colors)
        current = emptyList()
        history = emptyList()
        won = false
        lost = false
    }

    fun submit() {
        if (current.size != CODE_LENGTH || won || lost) return
        val (exact, partial) = codeFeedback(secret, current)
        history = history + GuessRecord(current, exact, partial)
        current = emptyList()
        if (exact == CODE_LENGTH) {
            won = true
            onWin(history.size)
        } else if (history.size >= difficulty.maxGuesses) {
            lost = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Code Breaker • ${difficulty.label}") },
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
                Text(
                    "Guess ${history.size + if (won || lost) 0 else 1}/${difficulty.maxGuesses}",
                    style = MaterialTheme.typography.titleMedium
                )
                when {
                    won -> Text("Cracked it!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    lost -> Text("Out of guesses!", color = Color.Red, fontWeight = FontWeight.Bold)
                    else -> {}
                }
                Button(onClick = ::reset) { Text("New Code") }
            }

            Spacer(Modifier.height(10.dp))

            // History (scrollable)
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                history.forEach { record ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        record.guess.forEach { colorIdx ->
                            Box(Modifier.size(30.dp).background(pegColors[colorIdx], CircleShape))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "● ${record.exact}   ○ ${record.partial}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (lost) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Code was:", style = MaterialTheme.typography.bodyMedium)
                        secret.forEach { colorIdx ->
                            Box(Modifier.size(26.dp).background(pegColors[colorIdx], CircleShape))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Current guess slots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until CODE_LENGTH) {
                    val filled = i < current.size
                    Box(
                        Modifier
                            .size(40.dp)
                            .then(
                                if (filled) Modifier.background(pegColors[current[i]], CircleShape)
                                else Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Palette
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (colorIdx in 0 until difficulty.colors) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .background(pegColors[colorIdx], CircleShape)
                            .clickable {
                                if (!won && !lost && current.size < CODE_LENGTH) current = current + colorIdx
                            }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { if (current.isNotEmpty()) current = current.dropLast(1) },
                    enabled = current.isNotEmpty() && !won && !lost
                ) { Text("Undo") }
                Button(
                    onClick = ::submit,
                    enabled = current.size == CODE_LENGTH && !won && !lost
                ) { Text("Check") }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "● right color, right spot   ○ right color, wrong spot",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
