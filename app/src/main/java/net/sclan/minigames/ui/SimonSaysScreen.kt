package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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

internal fun extendSimonSequence(sequence: List<Int>, random: Random = Random.Default): List<Int> =
    sequence + random.nextInt(4)

/** Completed rounds = sequence length the player fully repeated. */
internal fun simonScore(sequenceLength: Int, failed: Boolean): Int =
    if (failed) (sequenceLength - 1).coerceAtLeast(0) else sequenceLength

internal enum class SimonPhase { Idle, Showing, Input, GameOver }

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val padColors = listOf(
    Color(0xFF43A047), // green
    Color(0xFFE53935), // red
    Color(0xFFFDD835), // yellow
    Color(0xFF1E88E5)  // blue
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimonSaysScreen(
    speed: SimonSpeed = SimonSpeed.Normal,
    onBack: () -> Unit,
    onFinished: (rounds: Int) -> Unit = {}
) {
    var sequence by remember(speed) { mutableStateOf(listOf<Int>()) }
    var phase by remember(speed) { mutableStateOf(SimonPhase.Idle) }
    var inputIndex by remember(speed) { mutableIntStateOf(0) }
    var highlight by remember(speed) { mutableStateOf<Int?>(null) }
    var reported by remember(speed) { mutableStateOf(false) }

    LaunchedEffect(phase, sequence) {
        if (phase == SimonPhase.Showing) {
            delay(400)
            sequence.forEach { pad ->
                highlight = pad
                delay(speed.showMs)
                highlight = null
                delay(speed.gapMs)
            }
            inputIndex = 0
            phase = SimonPhase.Input
        }
    }

    fun start() {
        sequence = extendSimonSequence(emptyList())
        phase = SimonPhase.Showing
        inputIndex = 0
        highlight = null
        reported = false
    }

    fun tap(pad: Int) {
        when (phase) {
            SimonPhase.Idle, SimonPhase.GameOver -> {}
            SimonPhase.Showing -> {}
            SimonPhase.Input -> {
                if (pad == sequence[inputIndex]) {
                    inputIndex++
                    if (inputIndex == sequence.size) {
                        sequence = extendSimonSequence(sequence)
                        phase = SimonPhase.Showing
                    }
                } else {
                    phase = SimonPhase.GameOver
                    if (!reported) {
                        reported = true
                        onFinished(simonScore(sequence.size, failed = true))
                    }
                }
            }
        }
    }

    val roundsDone = if (phase == SimonPhase.GameOver) simonScore(sequence.size, true)
    else (sequence.size - 1).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simon Says • ${speed.label}") },
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
                Text("Rounds: $roundsDone", style = MaterialTheme.typography.titleMedium)
                when (phase) {
                    SimonPhase.Idle -> Text("Press Start", style = MaterialTheme.typography.bodySmall)
                    SimonPhase.Showing -> Text("Watch…", fontWeight = FontWeight.Bold)
                    SimonPhase.Input -> Text("Your turn!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    SimonPhase.GameOver -> Text("Wrong pad!", color = Color.Red, fontWeight = FontWeight.Bold)
                }
                Button(onClick = ::start) { Text(if (phase == SimonPhase.Idle) "Start" else "Restart") }
            }

            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (row in 0..1) {
                        Row(
                            Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0..1) {
                                val pad = row * 2 + col
                                val active = highlight == pad
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .background(
                                            if (active) padColors[pad] else padColors[pad].copy(alpha = 0.45f),
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clickable { tap(pad) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Watch the pads flash, then repeat the sequence by tapping them in order. One more pad is added every round.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
