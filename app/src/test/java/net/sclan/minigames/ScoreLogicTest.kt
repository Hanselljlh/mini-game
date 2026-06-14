package net.sclan.minigames

import net.sclan.minigames.data.ScoreLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreLogicTest {

    @Test fun `larger tile is better`() = assertTrue(ScoreLogic.isBetterTile(512, 256))
    @Test fun `smaller tile is not better`() = assertFalse(ScoreLogic.isBetterTile(256, 512))
    @Test fun `equal tile is not better`() = assertFalse(ScoreLogic.isBetterTile(256, 256))
    @Test fun `zero tile vs zero not better`() = assertFalse(ScoreLogic.isBetterTile(0, 0))

    @Test fun `higher score is better`() = assertTrue(ScoreLogic.isBetterScore(1000, 500))
    @Test fun `lower score is not better`() = assertFalse(ScoreLogic.isBetterScore(100, 500))
    @Test fun `equal score is not better`() = assertFalse(ScoreLogic.isBetterScore(500, 500))

    @Test fun `any time beats zero best`() = assertTrue(ScoreLogic.isBetterTime(120, 0))
    @Test fun `faster time is better`() = assertTrue(ScoreLogic.isBetterTime(45, 90))
    @Test fun `slower time is not better`() = assertFalse(ScoreLogic.isBetterTime(90, 45))
    @Test fun `equal time is not better`() = assertFalse(ScoreLogic.isBetterTime(60, 60))

    @Test fun `tileLabel zero shows dash`() = assertEquals("—", ScoreLogic.tileLabel(0))
    @Test fun `tileLabel shows value`() = assertEquals("2048", ScoreLogic.tileLabel(2048))

    @Test fun `timeLabel zero shows dash`() = assertEquals("—", ScoreLogic.timeLabel(0))
    @Test fun `timeLabel shows seconds`() = assertEquals("45s", ScoreLogic.timeLabel(45))
}
