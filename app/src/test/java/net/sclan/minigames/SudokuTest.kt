package net.sclan.minigames

import net.sclan.minigames.ui.SudokuDifficulty
import net.sclan.minigames.ui.generateSolvedSudoku
import net.sclan.minigames.ui.generateSudoku
import net.sclan.minigames.ui.sudokuConflicts
import net.sclan.minigames.ui.sudokuSolved
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuTest {

    private fun assertValidSolution(grid: List<List<Int>>, boxRows: Int, boxCols: Int) {
        val size = grid.size
        val expected = (1..size).toSet()
        // rows and columns
        for (i in 0 until size) {
            assertEquals("row $i", expected, grid[i].toSet())
            assertEquals("col $i", expected, (0 until size).map { grid[it][i] }.toSet())
        }
        // boxes
        for (br in 0 until size step boxRows) for (bc in 0 until size step boxCols) {
            val box = mutableSetOf<Int>()
            for (r in br until br + boxRows) for (c in bc until bc + boxCols) box.add(grid[r][c])
            assertEquals("box $br,$bc", expected, box)
        }
    }

    @Test fun solvedGridsAreValidForAllSizes() {
        SudokuDifficulty.entries.forEach { d ->
            repeat(3) { seed ->
                val grid = generateSolvedSudoku(d.size, d.boxRows, d.boxCols, Random(seed))
                assertValidSolution(grid, d.boxRows, d.boxCols)
            }
        }
    }

    @Test fun puzzleKeepsExactlyGivensCount() {
        SudokuDifficulty.entries.forEach { d ->
            val puzzle = generateSudoku(d, Random(11))
            val filled = puzzle.givens.sumOf { row -> row.count { it != 0 } }
            assertEquals(d.givens, filled)
        }
    }

    @Test fun givensMatchSolution() {
        val puzzle = generateSudoku(SudokuDifficulty.Midi, Random(5))
        for (r in puzzle.givens.indices) for (c in puzzle.givens.indices) {
            val g = puzzle.givens[r][c]
            if (g != 0) assertEquals(puzzle.solution[r][c], g)
        }
    }

    @Test fun solutionHasNoConflictsAndSolves() {
        val puzzle = generateSudoku(SudokuDifficulty.Mini, Random(2))
        assertTrue(sudokuConflicts(puzzle.solution, puzzle.boxRows, puzzle.boxCols).isEmpty())
        assertTrue(sudokuSolved(puzzle.solution, puzzle.boxRows, puzzle.boxCols))
    }

    @Test fun duplicateInRowIsConflict() {
        val puzzle = generateSudoku(SudokuDifficulty.Mini, Random(2))
        val bad = puzzle.solution.map { it.toMutableList() }
        bad[0][0] = bad[0][1] // duplicate in row 0
        val conflicts = sudokuConflicts(bad, puzzle.boxRows, puzzle.boxCols)
        assertTrue((0 to 0) in conflicts)
        assertTrue((0 to 1) in conflicts)
        assertFalse(sudokuSolved(bad, puzzle.boxRows, puzzle.boxCols))
    }

    @Test fun incompleteGridIsNotSolved() {
        val puzzle = generateSudoku(SudokuDifficulty.Mini, Random(2))
        assertFalse(sudokuSolved(puzzle.givens, puzzle.boxRows, puzzle.boxCols))
    }
}
