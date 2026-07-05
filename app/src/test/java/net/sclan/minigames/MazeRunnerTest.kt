package net.sclan.minigames

import net.sclan.minigames.ui.MazeDir
import net.sclan.minigames.ui.generateMaze
import net.sclan.minigames.ui.slideMaze
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MazeRunnerTest {

    @Test fun everyCellIsReachable() {
        repeat(5) { seed ->
            val maze = generateMaze(8, Random(seed))
            val visited = mutableSetOf(0 to 0)
            val queue = ArrayDeque(listOf(0 to 0))
            while (queue.isNotEmpty()) {
                val cell = queue.removeFirst()
                (maze.open[cell] ?: emptySet()).forEach { dir ->
                    val next = (cell.first + dir.dx) to (cell.second + dir.dy)
                    if (next !in visited) {
                        visited.add(next)
                        queue.add(next)
                    }
                }
            }
            assertEquals(64, visited.size)
        }
    }

    @Test fun passagesAreSymmetric() {
        val maze = generateMaze(8, Random(3))
        maze.open.forEach { (cell, dirs) ->
            dirs.forEach { dir ->
                val neighbor = (cell.first + dir.dx) to (cell.second + dir.dy)
                assertTrue(
                    "passage $cell -> $neighbor must be two-way",
                    dir.opposite in (maze.open[neighbor] ?: emptySet())
                )
            }
        }
    }

    @Test fun passagesStayInBounds() {
        val maze = generateMaze(8, Random(9))
        maze.open.forEach { (cell, dirs) ->
            dirs.forEach { dir ->
                val nx = cell.first + dir.dx
                val ny = cell.second + dir.dy
                assertTrue(nx in 0 until 8 && ny in 0 until 8)
            }
        }
    }

    @Test fun slideStopsAtWall() {
        val maze = generateMaze(8, Random(1))
        val end = slideMaze(maze, 0 to 0, MazeDir.E)
        // Wherever it stops, there must be a wall to the east
        assertTrue(MazeDir.E !in (maze.open[end] ?: emptySet()))
    }

    @Test fun slideIntoWallStaysPut() {
        val maze = generateMaze(8, Random(1))
        // Find a cell with a known closed side and confirm no movement
        val cell = 0 to 0
        MazeDir.entries.forEach { dir ->
            if (dir !in (maze.open[cell] ?: emptySet())) {
                assertEquals(cell, slideMaze(maze, cell, dir))
            }
        }
    }
}
