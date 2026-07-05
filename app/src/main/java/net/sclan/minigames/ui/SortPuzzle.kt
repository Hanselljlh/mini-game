package net.sclan.minigames.ui

import kotlin.random.Random

/**
 * Shared engine for pour-sorting puzzles (Water Sort, Nuts & Bolts).
 * A puzzle is a list of containers, each a stack of color ids (index 0 = bottom).
 */
data class SortState(
    val containers: List<List<Int>>,
    val capacity: Int = 4
)

/** Full containers of a single color, or empty, count as solved. */
fun sortSolved(state: SortState): Boolean = state.containers.all { c ->
    c.isEmpty() || (c.size == state.capacity && c.distinct().size == 1)
}

fun canPour(state: SortState, from: Int, to: Int): Boolean {
    if (from == to) return false
    val src = state.containers[from]
    val dst = state.containers[to]
    if (src.isEmpty() || dst.size >= state.capacity) return false
    return dst.isEmpty() || dst.last() == src.last()
}

/** Pours the top run of matching color, limited by destination space. */
fun pour(state: SortState, from: Int, to: Int): SortState? {
    if (!canPour(state, from, to)) return null
    val src = state.containers[from].toMutableList()
    val dst = state.containers[to].toMutableList()
    val color = src.last()
    var run = 0
    while (src.isNotEmpty() && src.last() == color && dst.size < state.capacity) {
        dst.add(src.removeAt(src.size - 1))
        run++
    }
    if (run == 0) return null
    return state.copy(containers = state.containers.mapIndexed { i, c ->
        when (i) {
            from -> src
            to -> dst
            else -> c
        }
    })
}

/**
 * Generates a shuffled puzzle: [colors] full containers worth of units dealt
 * randomly across [colors] containers plus [empties] empty ones.
 */
fun generateSortPuzzle(colors: Int, empties: Int = 2, capacity: Int = 4, random: Random = Random.Default): SortState {
    val units = (0 until colors).flatMap { color -> List(capacity) { color } }.shuffled(random)
    val filled = units.chunked(capacity)
    return SortState(containers = filled + List(empties) { emptyList() }, capacity = capacity)
}
