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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterSortScreen(
    difficulty: WaterSortDifficulty = WaterSortDifficulty.Normal,
    onBack: () -> Unit,
    onWin: (moves: Int) -> Unit = {}
) {
    var state by remember(difficulty) { mutableStateOf(generateSortPuzzle(difficulty.colors)) }
    var selected by remember(difficulty) { mutableStateOf<Int?>(null) }
    var moves by remember(difficulty) { mutableIntStateOf(0) }
    var won by remember(difficulty) { mutableStateOf(false) }

    fun reset() {
        state = generateSortPuzzle(difficulty.colors)
        selected = null
        moves = 0
        won = false
    }

    fun tapTube(index: Int) {
        if (won) return
        val sel = selected
        if (sel == null) {
            if (state.containers[index].isNotEmpty()) selected = index
            return
        }
        if (sel == index) {
            selected = null
            return
        }
        val next = pour(state, sel, index)
        selected = null
        if (next != null) {
            state = next
            moves++
            if (sortSolved(next)) {
                won = true
                onWin(moves)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Water Sort • ${difficulty.label}") },
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
                Text("Moves: $moves", style = MaterialTheme.typography.titleMedium)
                if (won) Text("Sorted! 🎉", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("New Puzzle") }
            }

            Spacer(Modifier.height(24.dp))

            val tubesPerRow = 5
            state.containers.indices.chunked(tubesPerRow).forEach { rowIdxs ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    rowIdxs.forEach { i ->
                        val tube = state.containers[i]
                        Column(
                            modifier = Modifier
                                .width(44.dp)
                                .border(
                                    3.dp,
                                    if (selected == i) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                                )
                                .padding(3.dp)
                                .clickable { tapTube(i) },
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            for (slot in state.capacity - 1 downTo 0) {
                                val color = tube.getOrNull(slot)
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .padding(vertical = 1.dp)
                                        .background(
                                            color?.let { sortColors[it % sortColors.size] } ?: Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Tap a tube to pick it up, then tap another to pour. A pour needs a matching color on top (or an empty tube). Sort every color into its own tube.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutsAndBoltsScreen(
    difficulty: WaterSortDifficulty = WaterSortDifficulty.Normal,
    onBack: () -> Unit,
    onWin: (moves: Int) -> Unit = {}
) {
    var state by remember(difficulty) { mutableStateOf(generateSortPuzzle(difficulty.colors)) }
    var selected by remember(difficulty) { mutableStateOf<Int?>(null) }
    var moves by remember(difficulty) { mutableIntStateOf(0) }
    var won by remember(difficulty) { mutableStateOf(false) }

    fun reset() {
        state = generateSortPuzzle(difficulty.colors)
        selected = null
        moves = 0
        won = false
    }

    fun tapBolt(index: Int) {
        if (won) return
        val sel = selected
        if (sel == null) {
            if (state.containers[index].isNotEmpty()) selected = index
            return
        }
        if (sel == index) {
            selected = null
            return
        }
        val next = pour(state, sel, index)
        selected = null
        if (next != null) {
            state = next
            moves++
            if (sortSolved(next)) {
                won = true
                onWin(moves)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuts & Bolts • ${difficulty.label}") },
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
                Text("Moves: $moves", style = MaterialTheme.typography.titleMedium)
                if (won) Text("All sorted! 🔧", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::reset) { Text("New Puzzle") }
            }

            Spacer(Modifier.height(24.dp))

            val boltsPerRow = 5
            state.containers.indices.chunked(boltsPerRow).forEach { rowIdxs ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowIdxs.forEach { i ->
                        val bolt = state.containers[i]
                        Column(
                            modifier = Modifier.clickable { tapBolt(i) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Bolt head
                            Box(
                                Modifier
                                    .size(width = 34.dp, height = 12.dp)
                                    .background(
                                        if (selected == i) MaterialTheme.colorScheme.primary else Color(0xFF90A4AE),
                                        RoundedCornerShape(3.dp)
                                    )
                            )
                            // Shaft with nuts (top slot first visually)
                            Box(contentAlignment = Alignment.TopCenter) {
                                Box(
                                    Modifier
                                        .width(10.dp)
                                        .height((state.capacity * 28 + 8).dp)
                                        .background(Color(0xFFB0BEC5))
                                )
                                Column {
                                    Spacer(Modifier.height(4.dp))
                                    for (slot in state.capacity - 1 downTo 0) {
                                        val color = bolt.getOrNull(slot)
                                        Box(
                                            Modifier
                                                .size(width = 40.dp, height = 24.dp)
                                                .padding(vertical = 2.dp)
                                                .background(
                                                    color?.let { sortColors[it % sortColors.size] } ?: Color.Transparent,
                                                    RoundedCornerShape(6.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Tap a bolt to grab its top nuts, then tap another bolt to screw them on. Nuts only stack on the same color or a bare bolt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
