package net.sclan.minigames

import net.sclan.minigames.ui.GameId
import net.sclan.minigames.ui.MinesweeperDifficulty
import net.sclan.minigames.ui.TicTacToeDifficulty
import net.sclan.minigames.ui.TileMergeDifficulty
import net.sclan.minigames.ui.chooseSmartBotMove
import net.sclan.minigames.ui.createMsBoard
import net.sclan.minigames.ui.defaultSetupChoice
import net.sclan.minigames.ui.startingBoard2048
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyConfigTest {
    @Test fun minesweeperDifficultiesIncreaseBoardAndMines() {
        assertTrue(MinesweeperDifficulty.Easy.config.mines < MinesweeperDifficulty.Normal.config.mines)
        assertTrue(MinesweeperDifficulty.Normal.config.mines < MinesweeperDifficulty.Hard.config.mines)
        assertTrue(MinesweeperDifficulty.Hard.config.rows > MinesweeperDifficulty.Normal.config.rows)
    }

    @Test fun customMinesweeperBoardUsesSelectedDifficulty() {
        val config = MinesweeperDifficulty.Hard.config
        val board = createMsBoard(avoidRow = 0, avoidCol = 0, config = config)
        assertEquals(config.rows, board.size)
        assertEquals(config.cols, board.first().size)
        assertEquals(config.mines, board.sumOf { row -> row.count { it.isMine } })
        assertFalse(board[0][0].isMine)
    }

    @Test fun tileMergeStartingTilesFollowDifficulty() {
        TileMergeDifficulty.entries.forEach { difficulty ->
            val board = startingBoard2048(difficulty)
            assertEquals(difficulty.startTiles, board.sumOf { row -> row.count { it != 0 } })
        }
    }

    @Test fun defaultSetupChoiceMatchesGame() {
        assertEquals(TileMergeDifficulty.Normal, defaultSetupChoice(GameId.TileMerge).tileMerge)
        assertEquals(MinesweeperDifficulty.Normal, defaultSetupChoice(GameId.Minesweeper).minesweeper)
        assertEquals(TicTacToeDifficulty.TwoPlayer, defaultSetupChoice(GameId.TicTacToe).ticTacToe)
    }

    @Test fun smartBotWinsBeforeBlocking() {
        val board = listOf("O", "O", "", "X", "X", "", "", "", "")
        assertEquals(2, chooseSmartBotMove(board))
    }

    @Test fun smartBotBlocksHumanThreat() {
        val board = listOf("X", "X", "", "O", "", "", "", "", "")
        assertEquals(2, chooseSmartBotMove(board))
    }
}
