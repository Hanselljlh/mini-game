package net.sclan.minigames.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
fun BubbleWrapScreen(
    size: BubbleWrapSize = BubbleWrapSize.Standard,
    onBack: () -> Unit,
    onSheetDone: () -> Unit = {}
) {
    var popped by remember(size) { mutableStateOf(setOf<Int>()) }
    var sheets by remember(size) { mutableIntStateOf(0) }
    val total = size.rows * size.cols
    val done = popped.size == total

    fun pop(index: Int) {
        if (index in popped) return
        popped = popped + index
        if (popped.size == total) {
            sheets++
            onSheetDone()
        }
    }

    fun newSheet() {
        popped = emptySet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bubble Wrap • ${size.label}") },
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
                Text("Popped ${popped.size}/$total", style = MaterialTheme.typography.titleMedium)
                if (sheets > 0) Text("Sheets: $sheets", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = ::newSheet) { Text("New Sheet") }
            }

            Spacer(Modifier.height(12.dp))

            if (done) {
                Text(
                    "Sheet complete. Very satisfying. 😌",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                    for (r in 0 until size.rows) {
                        Row(
                            Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (c in 0 until size.cols) {
                                val index = r * size.cols + c
                                val isPopped = index in popped
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(3.dp)
                                        .background(
                                            if (isPopped) Color(0xFFB0BEC5).copy(alpha = 0.45f)
                                            else Color(0xFF90CAF9),
                                            CircleShape
                                        )
                                        .clickable { pop(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPopped) {
                                        Text("·", color = Color(0xFF546E7A))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "No score. No timer. Just pop.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
