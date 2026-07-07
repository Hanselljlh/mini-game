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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic
// ---------------------------------------------------------------------------

internal fun averageMs(times: List<Long>): Long =
    if (times.isEmpty()) 0L else times.sum() / times.size

internal fun reactionRating(avgMs: Long): String = when {
    avgMs <= 0L -> "—"
    avgMs < 250L -> "Lightning ⚡"
    avgMs < 350L -> "Quick"
    avgMs < 500L -> "Steady"
    else -> "Warming Up"
}

internal enum class ReactionPhase { Idle, Waiting, Go, TooSoon, Finished }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionTapScreen(
    mode: ReactionTapMode = ReactionTapMode.Standard,
    onBack: () -> Unit,
    onFinish: (avgMs: Long) -> Unit = {}
) {
    var phase by remember(mode) { mutableStateOf(ReactionPhase.Idle) }
    var round by remember(mode) { mutableIntStateOf(0) }
    var times by remember(mode) { mutableStateOf(listOf<Long>()) }
    var goAt by remember(mode) { mutableLongStateOf(0L) }
    var lastMs by remember(mode) { mutableLongStateOf(0L) }
    var reported by remember(mode) { mutableStateOf(false) }

    LaunchedEffect(phase, round) {
        if (phase == ReactionPhase.Waiting) {
            delay(Random.nextLong(1200, 3200))
            goAt = System.currentTimeMillis()
            phase = ReactionPhase.Go
        }
    }

    fun reset() {
        phase = ReactionPhase.Idle
        round = 0
        times = emptyList()
        goAt = 0L
        lastMs = 0L
        reported = false
    }

    fun tap() {
        when (phase) {
            ReactionPhase.Idle, ReactionPhase.TooSoon -> phase = ReactionPhase.Waiting
            ReactionPhase.Waiting -> phase = ReactionPhase.TooSoon
            ReactionPhase.Go -> {
                val ms = System.currentTimeMillis() - goAt
                lastMs = ms
                times = times + ms
                round++
                if (round >= mode.rounds) {
                    phase = ReactionPhase.Finished
                    if (!reported) {
                        reported = true
                        onFinish(averageMs(times))
                    }
                } else {
                    phase = ReactionPhase.Waiting
                }
            }
            ReactionPhase.Finished -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reaction Tap • ${mode.rounds} rounds") },
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
                Text("Round ${round.coerceAtMost(mode.rounds)}/${mode.rounds}", style = MaterialTheme.typography.titleMedium)
                if (lastMs > 0L && phase != ReactionPhase.Finished) Text("Last: $lastMs ms")
                Button(onClick = ::reset) { Text("Restart") }
            }

            Spacer(Modifier.height(16.dp))

            val (bg, label, sub) = when (phase) {
                ReactionPhase.Idle -> Triple(MaterialTheme.colorScheme.primaryContainer, "Tap to start", "Wait for the panel to turn green, then tap fast.")
                ReactionPhase.Waiting -> Triple(Color(0xFFC62828), "Wait…", "Don't tap until it turns green!")
                ReactionPhase.Go -> Triple(Color(0xFF2E7D32), "TAP!", "")
                ReactionPhase.TooSoon -> Triple(Color(0xFFF9A825), "Too soon!", "Tap to try that round again.")
                ReactionPhase.Finished -> Triple(MaterialTheme.colorScheme.secondaryContainer, "${averageMs(times)} ms average", reactionRating(averageMs(times)))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(bg, RoundedCornerShape(16.dp))
                    .clickable { tap() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (phase == ReactionPhase.Waiting || phase == ReactionPhase.Go) Color.White
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (sub.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            sub,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (phase == ReactionPhase.Waiting) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (phase == ReactionPhase.Finished) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = ::reset) { Text("Play Again") }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Lower average = better. Your best average is saved to your local profile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
