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
import androidx.compose.runtime.mutableFloatStateOf
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
import kotlinx.coroutines.delay
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Pure game logic — timing-based penalty shootout, 5 shots.
// The aim marker sweeps 0..1; its position maps to a goal zone.
// ---------------------------------------------------------------------------

internal const val PK_SHOTS = 5

internal fun pkZoneFor(position: Float): Int = when {
    position < 1f / 3 -> 0 // left
    position < 2f / 3 -> 1 // center
    else -> 2              // right
}

internal fun pkKeeperPick(random: Random = Random.Default): Int = random.nextInt(3)

/** A shot scores when the keeper dives to a different zone. */
internal fun pkIsGoal(shotZone: Int, keeperZone: Int): Boolean = shotZone != keeperZone

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

private val pkZoneNames = listOf("LEFT", "CENTER", "RIGHT")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenaltyKicksScreen(
    onBack: () -> Unit,
    onFinished: (goals: Int) -> Unit = {}
) {
    var marker by remember { mutableFloatStateOf(0f) }
    var sweeping by remember { mutableStateOf(false) }
    var direction by remember { mutableFloatStateOf(1f) }
    var shotsTaken by remember { mutableIntStateOf(0) }
    var goals by remember { mutableIntStateOf(0) }
    var lastResult by remember { mutableStateOf("Tap Kick to aim, tap Shoot to fire!") }
    var done by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf(false) }

    LaunchedEffect(sweeping) {
        while (sweeping) {
            delay(16)
            var next = marker + direction * 0.03f
            if (next >= 1f) { next = 1f; direction = -1f }
            if (next <= 0f) { next = 0f; direction = 1f }
            marker = next
        }
    }

    fun reset() {
        marker = 0f
        sweeping = false
        shotsTaken = 0
        goals = 0
        lastResult = "Tap Kick to aim, tap Shoot to fire!"
        done = false
        reported = false
    }

    fun kickOrShoot() {
        if (done) return
        if (!sweeping) {
            sweeping = true
            lastResult = "Aim sweeping — tap Shoot!"
            return
        }
        sweeping = false
        val shotZone = pkZoneFor(marker)
        val keeperZone = pkKeeperPick()
        val goal = pkIsGoal(shotZone, keeperZone)
        if (goal) goals++
        shotsTaken++
        lastResult = if (goal)
            "GOAL! You shot ${pkZoneNames[shotZone]}, keeper dove ${pkZoneNames[keeperZone]}. ⚽"
        else
            "SAVED! Keeper guessed ${pkZoneNames[keeperZone]}."
        if (shotsTaken >= PK_SHOTS) {
            done = true
            if (!reported) {
                reported = true
                onFinished(goals)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Penalty Kicks") },
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
                Text("Shot ${shotsTaken.coerceAtMost(PK_SHOTS)}/$PK_SHOTS", style = MaterialTheme.typography.titleMedium)
                Text("Goals: $goals", fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("New Round") }
            }

            Spacer(Modifier.height(20.dp))

            // Goal mouth with three zones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFF37474F), RoundedCornerShape(8.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val activeZone = if (sweeping) pkZoneFor(marker) else -1
                for (zone in 0..2) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(
                                if (zone == activeZone) Color(0xFF81C784) else Color(0xFF546E7A),
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(pkZoneNames[zone], color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Sweep bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                Spacer(Modifier.weight(marker.coerceAtLeast(0.001f)))
                Box(
                    Modifier
                        .weight(0.06f)
                        .height(16.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.weight((1f - marker).coerceAtLeast(0.001f)))
            }

            Spacer(Modifier.height(20.dp))
            Text(lastResult, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            if (done) {
                Text(
                    when {
                        goals >= 4 -> "Clinical! $goals/$PK_SHOTS ⚽🔥"
                        goals >= 3 -> "Solid shooting: $goals/$PK_SHOTS"
                        else -> "Tough keeper today: $goals/$PK_SHOTS"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = ::reset) { Text("Shoot Again") }
            } else {
                Button(onClick = ::kickOrShoot, modifier = Modifier.fillMaxWidth()) {
                    Text(if (sweeping) "SHOOT!" else "Kick")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "The marker sweeps across the goal. Shoot to lock your corner — score when the keeper dives the wrong way. 5 shots per round.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
