package net.sclan.minigames

import net.sclan.minigames.ui.BF_SHAPES
import net.sclan.minigames.ui.BF_SIZE
import net.sclan.minigames.ui.ESC_EXIT_ROW
import net.sclan.minigames.ui.ESC_FALLBACK
import net.sclan.minigames.ui.ESC_SIZE
import net.sclan.minigames.ui.EscBlock
import net.sclan.minigames.ui.EscapeDifficulty
import net.sclan.minigames.ui.escMinMoves
import net.sclan.minigames.ui.generateEscapePuzzle
import net.sclan.minigames.ui.FLAPPY_BIRD_R
import net.sclan.minigames.ui.FlappyState
import net.sclan.minigames.ui.MazeDir
import net.sclan.minigames.ui.SAND_COLS
import net.sclan.minigames.ui.SAND_ROWS
import net.sclan.minigames.ui.bfCanPlace
import net.sclan.minigames.ui.bfPlace
import net.sclan.minigames.ui.bfRandomPieces
import net.sclan.minigames.ui.dropSand
import net.sclan.minigames.ui.emptySand
import net.sclan.minigames.ui.escMove
import net.sclan.minigames.ui.escOccupied
import net.sclan.minigames.ui.escSolved
import net.sclan.minigames.ui.flappyJump
import net.sclan.minigames.ui.flappyTick
import net.sclan.minigames.ui.generateMaze
import net.sclan.minigames.ui.sandIndex
import net.sclan.minigames.ui.slidePaintMaze
import net.sclan.minigames.ui.stepSand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ArcadePackTest {

    // --- Escape (procedural) ---

    @Test fun generatedPuzzlesAreWellFormedAndSolvable() {
        EscapeDifficulty.entries.forEach { d ->
            repeat(4) { seed ->
                val (blocks, par) = generateEscapePuzzle(d, Random(seed * 31 + d.ordinal), attempts = 120)
                val red = blocks.first()
                assertEquals("red id", 0, red.id)
                assertTrue("red horizontal", red.horizontal)
                assertEquals("red on exit row", ESC_EXIT_ROW, red.row)
                val cells = blocks.flatMap { it.cells() }
                assertEquals("no overlaps", cells.size, cells.toSet().size)
                cells.forEach { (r, c) ->
                    assertTrue("in bounds", r in 0 until ESC_SIZE && c in 0 until ESC_SIZE)
                }
                // Par is the proven BFS solution length — puzzle must be solvable
                assertEquals("par matches solver", escMinMoves(blocks), par)
                assertTrue("not pre-solved", par >= 1)
            }
        }
    }

    @Test fun casualPuzzlesLandInBand() {
        // The easy band is dense in random space — generation should hit it.
        repeat(5) { seed ->
            val (_, par) = generateEscapePuzzle(EscapeDifficulty.Casual, Random(seed), attempts = 150)
            assertTrue("casual par $par", par in 1..9) // band 3..7 plus small fallback slack
        }
    }

    @Test fun puzzlesVaryAcrossCalls() {
        val layouts = (0 until 6).map { seed ->
            generateEscapePuzzle(EscapeDifficulty.Casual, Random(seed), attempts = 60).first
        }
        assertTrue("distinct layouts", layouts.toSet().size >= 4)
    }

    @Test fun fallbackPuzzleIsSolvable() {
        val par = escMinMoves(ESC_FALLBACK)
        assertTrue(par != null && par in 1..10)
    }

    @Test fun minMovesDetectsUnsolvable() {
        // Red walled in by a full-height column it can never pass: verticals of
        // length 3 stacked at col 5 can only slide within the column — unsolvable.
        val blocked = listOf(
            EscBlock(0, ESC_EXIT_ROW, 0, 2, true),
            EscBlock(1, 0, 5, 3, false),
            EscBlock(2, 3, 5, 3, false)
        )
        assertNull(escMinMoves(blocked))
    }

    @Test fun escMoveRejectsCollisionsAndBounds() {
        val blocks = listOf(
            EscBlock(0, 2, 0, 2, true),
            EscBlock(1, 2, 2, 2, false)
        )
        assertNull(escMove(blocks, 0, 1)) // red would hit the vertical block
        assertNull(escMove(blocks, 0, -1)) // red at left wall
        assertTrue(escOccupied(blocks).contains(2 to 2))
    }

    // --- Maze Paint ---

    @Test fun slidePaintReturnsFullPath() {
        val maze = generateMaze(8, Random(4))
        val path = slidePaintMaze(maze, 0 to 0, MazeDir.S)
        assertEquals(0 to 0, path.first())
        // Path is contiguous
        path.zipWithNext().forEach { (a, b) ->
            assertEquals(1, kotlin.math.abs(a.first - b.first) + kotlin.math.abs(a.second - b.second))
        }
    }

    // --- Flappy ---

    @Test fun gravityPullsBirdDown() {
        val s0 = FlappyState()
        val s1 = flappyTick(s0, speed = 0.008f, gap = 0.3f, random = Random(1))
        assertTrue(s1.birdY > s0.birdY)
    }

    @Test fun jumpSetsUpwardVelocity() {
        val s = flappyJump(FlappyState())
        assertTrue(s.velocity < 0)
    }

    @Test fun fallingForeverDies() {
        var s = FlappyState()
        repeat(200) { s = flappyTick(s, 0.008f, 0.3f, Random(2)) }
        assertFalse(s.alive)
    }

    @Test fun pipesSpawnAndScroll() {
        var s = FlappyState(birdY = 0.5f, velocity = 0f)
        s = flappyTick(s, 0.008f, 0.3f, Random(3))
        assertTrue(s.pipes.isNotEmpty())
        val x0 = s.pipes.first().x
        val s2 = flappyTick(s, 0.008f, 0.3f, Random(4))
        assertTrue(s2.pipes.first().x < x0)
    }

    @Test fun deadBirdStaysDead() {
        val dead = FlappyState(alive = false, birdY = 2f)
        assertEquals(dead, flappyTick(dead, 0.008f, 0.3f))
        assertEquals(dead, flappyJump(dead))
    }

    // --- Sand ---

    @Test fun sandFallsStraightDown() {
        val grid = emptySand()
        grid[sandIndex(10, 0)] = 3
        val next = stepSand(grid, Random(1))
        assertEquals(-1, next[sandIndex(10, 0)])
        assertEquals(3, next[sandIndex(10, 1)])
    }

    @Test fun sandPilesOnFloor() {
        var grid = emptySand()
        grid[sandIndex(5, SAND_ROWS - 1)] = 1
        grid[sandIndex(5, SAND_ROWS - 2)] = 1
        val next = stepSand(grid, Random(1))
        // Bottom grain stays; grain above either stays stacked or slides diagonally down
        assertEquals(1, next[sandIndex(5, SAND_ROWS - 1)])
        val secondGrain = listOf(
            next[sandIndex(5, SAND_ROWS - 2)],
            next[sandIndex(4, SAND_ROWS - 1)],
            next[sandIndex(6, SAND_ROWS - 1)]
        )
        assertTrue(secondGrain.count { it == 1 } == 1)
    }

    @Test fun dropSandRespectsBounds() {
        val grid = emptySand()
        val added = dropSand(grid, 0, 0, brush = 2, colorIdx = 5)
        assertTrue(added in 1..(5 * 5))
        assertTrue(grid.count { it >= 0 } == added)
        assertTrue(grid.indices.all { it < SAND_COLS * SAND_ROWS })
    }

    // --- Block Fill ---

    @Test fun allShapesFitEmptyBoard() {
        BF_SHAPES.forEachIndexed { i, shape ->
            assertTrue("shape $i", bfCanPlace(emptySet(), shape, 0, 0))
        }
    }

    @Test fun placementRejectsOverlapAndBounds() {
        val square = BF_SHAPES[5] // 2×2
        val (board, _) = bfPlace(emptySet(), square, 0, 0)
        assertFalse(bfCanPlace(board, square, 0, 0))
        assertFalse(bfCanPlace(board, square, 1, 1)) // overlaps corner
        assertFalse(bfCanPlace(emptySet(), square, BF_SIZE - 1, BF_SIZE - 1)) // out of bounds
    }

    @Test fun fullRowClears() {
        // Fill row 0 except last cell, then place a dot to complete it
        var board = (0 until BF_SIZE - 1).map { 0 to it }.toSet()
        val (next, cleared) = bfPlace(board, BF_SHAPES[0], 0, BF_SIZE - 1)
        assertEquals(BF_SIZE, cleared)
        assertTrue(next.none { it.first == 0 })
    }

    @Test fun rowAndColumnComboClears() {
        // Row 0 missing only its last cell, and column 0 otherwise full:
        // the dot at (0,9) completes BOTH lines → 19 distinct cells clear.
        val board = ((0 until BF_SIZE - 1).map { 0 to it } + (1 until BF_SIZE).map { it to 0 }).toSet()
        val (after, cleared) = bfPlace(board, BF_SHAPES[0], 0, BF_SIZE - 1)
        assertEquals(2 * BF_SIZE - 1, cleared)
        assertTrue(after.isEmpty())
    }

    @Test fun randomPiecesAreValidIndices() {
        repeat(20) { seed ->
            bfRandomPieces(random = Random(seed)).forEach { idx ->
                assertTrue(idx in BF_SHAPES.indices)
            }
        }
    }
}
