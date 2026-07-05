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
// Pure game logic — "cross math" puzzle:
//   a op1 b = r1
//  op3    op4
//   c op2 d = r2
//   =      =
//   r3     r4
// Player fills a, b, c, d from a tile tray (with decoys on harder modes).
// ---------------------------------------------------------------------------

internal data class CrossMathPuzzle(
    val a: Int, val b: Int, val c: Int, val d: Int,
    val op1: Char, val op2: Char, val op3: Char, val op4: Char,
    val r1: Int, val r2: Int, val r3: Int, val r4: Int,
    val tray: List<Int>
)

internal fun crossApply(x: Int, op: Char, y: Int): Int = when (op) {
    '+' -> x + y
    '−' -> x - y
    '×' -> x * y
    else -> error("bad op")
}

internal fun generateCrossMath(ops: List<Char>, decoys: Int, random: Random = Random.Default): CrossMathPuzzle {
    while (true) {
        val nums = (1..9).shuffled(random)
        val a = nums[0]; val b = nums[1]; val c = nums[2]; val d = nums[3]
        val op1 = ops[random.nextInt(ops.size)]
        val op2 = ops[random.nextInt(ops.size)]
        val op3 = ops[random.nextInt(ops.size)]
        val op4 = ops[random.nextInt(ops.size)]
        val r1 = crossApply(a, op1, b)
        val r2 = crossApply(c, op2, d)
        val r3 = crossApply(a, op3, c)
        val r4 = crossApply(b, op4, d)
        // Keep results friendly: no negatives, cap products
        if (listOf(r1, r2, r3, r4).any { it < 0 || it > 81 }) continue
        val decoyValues = (1..9).filter { it != a && it != b && it != c && it != d }
            .shuffled(random).take(decoys)
        return CrossMathPuzzle(
            a, b, c, d, op1, op2, op3, op4, r1, r2, r3, r4,
            tray = (listOf(a, b, c, d) + decoyValues).shuffled(random)
        )
    }
}

/** Checks a full assignment against all four equations. */
internal fun crossMathSolved(p: CrossMathPuzzle, va: Int?, vb: Int?, vc: Int?, vd: Int?): Boolean {
    if (va == null || vb == null || vc == null || vd == null) return false
    return crossApply(va, p.op1, vb) == p.r1 &&
        crossApply(vc, p.op2, vd) == p.r2 &&
        crossApply(va, p.op3, vc) == p.r3 &&
        crossApply(vb, p.op4, vd) == p.r4
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossMathScreen(
    difficulty: CrossMathDifficulty = CrossMathDifficulty.Normal,
    onBack: () -> Unit,
    onWin: () -> Unit = {}
) {
    var puzzle by remember(difficulty) { mutableStateOf(generateCrossMath(difficulty.ops, difficulty.decoys)) }
    var slots by remember(difficulty) { mutableStateOf(listOf<Int?>(null, null, null, null)) } // a, b, c, d
    var selectedSlot by remember(difficulty) { mutableIntStateOf(0) }
    var solvedCount by remember(difficulty) { mutableIntStateOf(0) }
    var won by remember(difficulty) { mutableStateOf(false) }

    fun newPuzzle() {
        puzzle = generateCrossMath(difficulty.ops, difficulty.decoys)
        slots = listOf(null, null, null, null)
        selectedSlot = 0
        won = false
    }

    fun placeValue(v: Int) {
        if (won) return
        val next = slots.toMutableList()
        next[selectedSlot] = v
        slots = next
        selectedSlot = (selectedSlot + 1).coerceAtMost(3)
        if (crossMathSolved(puzzle, next[0], next[1], next[2], next[3])) {
            won = true
            solvedCount++
            onWin()
        }
    }

    val usedCounts = slots.filterNotNull().groupingBy { it }.eachCount()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cross Math • ${difficulty.label}") },
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
                Text("Solved: $solvedCount", style = MaterialTheme.typography.titleMedium)
                if (won) Text("Correct! ✓", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Button(onClick = ::newPuzzle) { Text(if (won) "Next Puzzle" else "New Puzzle") }
            }

            Spacer(Modifier.height(24.dp))

            @Composable
            fun ValueSlot(index: Int) {
                val value = slots[index]
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (value != null) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            3.dp,
                            if (selectedSlot == index && !won) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedSlot = index
                            if (value != null) slots = slots.toMutableList().also { it[index] = null }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(value?.toString() ?: "?", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            @Composable
            fun Fixed(text: String) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Text(text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ValueSlot(0); Fixed(puzzle.op1.toString()); ValueSlot(1); Fixed("="); Fixed(puzzle.r1.toString())
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Fixed(puzzle.op3.toString()); Fixed(""); Fixed(puzzle.op4.toString()); Fixed(""); Fixed("")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ValueSlot(2); Fixed(puzzle.op2.toString()); ValueSlot(3); Fixed("="); Fixed(puzzle.r2.toString())
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Fixed("="); Fixed(""); Fixed("="); Fixed(""); Fixed("")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Fixed(puzzle.r3.toString()); Fixed(""); Fixed(puzzle.r4.toString()); Fixed(""); Fixed("")
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                puzzle.tray.forEachIndexed { i, v ->
                    val available = (usedCounts[v] ?: 0) < puzzle.tray.count { it == v } &&
                        (usedCounts[v] ?: 0) == 0 // each tray tile usable once; values are distinct
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (available) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = available && !won) { placeValue(v) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(v.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "Fill the four ? squares so every equation works — across AND down. Tap a filled square to take its number back.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
