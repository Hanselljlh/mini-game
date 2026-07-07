package net.sclan.minigames.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs

// ---------------------------------------------------------------------------
// FIDGET SPINNER — flick to spin, friction slows it down
// ---------------------------------------------------------------------------

/** Applies one friction tick; returns (newAngle, newVelocity). Pure for tests. */
internal fun spinnerTick(angle: Float, velocity: Float): Pair<Float, Float> {
    val newVelocity = velocity * 0.985f
    return (angle + newVelocity) % 360f to if (abs(newVelocity) < 0.05f) 0f else newVelocity
}

internal fun spinnerTotalRotations(accumulated: Float): Int = (abs(accumulated) / 360f).toInt()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FidgetSpinnerScreen(
    onBack: () -> Unit
) {
    var angle by remember { mutableFloatStateOf(0f) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var accumulated by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            if (velocity != 0f) {
                val (a, v) = spinnerTick(angle, velocity)
                accumulated += abs(velocity)
                angle = a
                velocity = v
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fidget Spinner") },
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
                Text("Spins: ${spinnerTotalRotations(accumulated)}", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { velocity = 0f; accumulated = 0f }) { Text("Reset") }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Flick: horizontal drag at the top spins one way, at the bottom the other
                            val sign = if (change.position.y < size.height / 2) 1f else -1f
                            velocity = (velocity + sign * dragAmount.x * 0.08f).coerceIn(-60f, 60f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = this.size.width / 2
                    val cy = this.size.height / 2
                    val armLen = minOf(cx, cy) * 0.55f
                    rotate(angle, pivot = Offset(cx, cy)) {
                        val colors = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047))
                        for (i in 0..2) {
                            rotate(i * 120f, pivot = Offset(cx, cy)) {
                                drawCircle(colors[i], radius = armLen * 0.38f, center = Offset(cx, cy - armLen))
                                drawLine(
                                    Color(0xFF455A64),
                                    start = Offset(cx, cy),
                                    end = Offset(cx, cy - armLen),
                                    strokeWidth = armLen * 0.3f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        drawCircle(Color(0xFF263238), radius = armLen * 0.28f, center = Offset(cx, cy))
                        drawCircle(Color(0xFF90A4AE), radius = armLen * 0.12f, center = Offset(cx, cy))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Flick left or right to spin — swipe across the top or bottom for opposite directions. Then just watch it go.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// CHALK DOODLE — free drawing with cycling chalk colors
// ---------------------------------------------------------------------------

private val chalkColors = listOf(
    Color(0xFFFFF9C4), Color(0xFFB3E5FC), Color(0xFFF8BBD0), Color(0xFFC8E6C9),
    Color(0xFFFFE0B2), Color(0xFFE1BEE7), Color.White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChalkDoodleScreen(
    onBack: () -> Unit
) {
    var strokes by remember { mutableStateOf(listOf<Pair<List<Offset>, Color>>()) }
    var currentStroke by remember { mutableStateOf(listOf<Offset>()) }
    var colorIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chalk Doodle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    chalkColors.forEachIndexed { i, color ->
                        Box(
                            Modifier
                                .height(28.dp)
                                .width(30.dp)
                                .background(color, RoundedCornerShape(6.dp))
                                .border(
                                    2.dp,
                                    if (colorIndex == i) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { colorIndex = i }
                        )
                    }
                }
                Button(onClick = { strokes = emptyList(); currentStroke = emptyList() }) { Text("Wipe") }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF2E3B32), RoundedCornerShape(12.dp))
                    .pointerInput(colorIndex) {
                        detectDragGestures(
                            onDragStart = { pos -> currentStroke = listOf(pos) },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke = currentStroke + change.position
                            },
                            onDragEnd = {
                                if (currentStroke.size > 1) {
                                    strokes = strokes + (currentStroke to chalkColors[colorIndex])
                                }
                                currentStroke = emptyList()
                            }
                        )
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    fun drawStroke(points: List<Offset>, color: Color) {
                        if (points.size < 2) return
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, color.copy(alpha = 0.85f), style = Stroke(width = 9f, cap = StrokeCap.Round))
                    }
                    strokes.forEach { (points, color) -> drawStroke(points, color) }
                    drawStroke(currentStroke, chalkColors[colorIndex])
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Pick a chalk color and draw on the board. Wipe clean whenever you like. That's it — it's a chalkboard.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
