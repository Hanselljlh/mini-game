package net.sclan.minigames

import net.sclan.minigames.ui.ChessBoard
import net.sclan.minigames.ui.ChessState
import net.sclan.minigames.ui.ChessStatus
import net.sclan.minigames.ui.Piece
import net.sclan.minigames.ui.PieceColor
import net.sclan.minigames.ui.PieceType
import net.sclan.minigames.ui.applyChessMove
import net.sclan.minigames.ui.chessBestMove
import net.sclan.minigames.ui.chessStatus
import net.sclan.minigames.ui.initialChessState
import net.sclan.minigames.ui.isInCheck
import net.sclan.minigames.ui.legalMoves
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessTest {

    private fun boardOf(vararg placements: Triple<Int, Int, Piece>): ChessBoard {
        val b = MutableList(8) { MutableList<Piece?>(8) { null } }
        placements.forEach { (r, c, p) -> b[r][c] = p }
        return b.map { it.toList() }
    }

    private fun wk(r: Int, c: Int) = Triple(r, c, Piece(PieceType.King, PieceColor.White))
    private fun bk(r: Int, c: Int) = Triple(r, c, Piece(PieceType.King, PieceColor.Black))

    @Test fun openingHasTwentyLegalMoves() {
        assertEquals(20, legalMoves(initialChessState()).size)
    }

    @Test fun pawnDoubleStepSetsEnPassant() {
        var s = initialChessState()
        val e2e4 = legalMoves(s).first { it.from == (6 to 4) && it.to == (4 to 4) }
        s = applyChessMove(s, e2e4)
        assertEquals(5 to 4, s.enPassant)
        assertEquals(PieceColor.Black, s.toMove)
    }

    @Test fun enPassantCaptureRemovesPawn() {
        // White pawn e5 (3,4); black pawn d5 (3,3) just moved two → en passant target d6 (2,3)
        val s = ChessState(
            boardOf(
                wk(7, 4), bk(0, 4),
                Triple(3, 4, Piece(PieceType.Pawn, PieceColor.White)),
                Triple(3, 3, Piece(PieceType.Pawn, PieceColor.Black))
            ),
            toMove = PieceColor.White,
            castling = emptySet(),
            enPassant = 2 to 3
        )
        val ep = legalMoves(s).first { it.from == (3 to 4) && it.to == (2 to 3) }
        assertTrue(ep.isEnPassant)
        val next = applyChessMove(s, ep)
        assertNull(next.board[3][3])                 // captured pawn gone
        assertNotNull(next.board[2][3])              // capturing pawn advanced
    }

    @Test fun kingsideCastlingWorks() {
        val s = ChessState(
            boardOf(
                wk(7, 4), bk(0, 4),
                Triple(7, 7, Piece(PieceType.Rook, PieceColor.White))
            ),
            toMove = PieceColor.White,
            castling = setOf("WK")
        )
        val castle = legalMoves(s).firstOrNull { it.isCastle && it.to == (7 to 6) }
        assertNotNull(castle)
        val next = applyChessMove(s, castle!!)
        assertEquals(PieceType.King, next.board[7][6]?.type)
        assertEquals(PieceType.Rook, next.board[7][5]?.type)
        assertNull(next.board[7][4])
        assertNull(next.board[7][7])
    }

    @Test fun cannotCastleThroughCheck() {
        // Black rook on f8 (0,5) attacks f1 (7,5) — king may not pass through it.
        val s = ChessState(
            boardOf(
                wk(7, 4), bk(0, 0),
                Triple(7, 7, Piece(PieceType.Rook, PieceColor.White)),
                Triple(0, 5, Piece(PieceType.Rook, PieceColor.Black))
            ),
            toMove = PieceColor.White,
            castling = setOf("WK")
        )
        assertTrue(legalMoves(s).none { it.isCastle })
    }

    @Test fun checkIsDetected() {
        // Black rook on the e-file with a clear path down to the white king.
        val s = ChessState(
            boardOf(wk(7, 4), bk(0, 0), Triple(2, 4, Piece(PieceType.Rook, PieceColor.Black))),
            toMove = PieceColor.White
        )
        assertTrue(isInCheck(s, PieceColor.White))
        assertFalse(isInCheck(s, PieceColor.Black))
    }

    @Test fun pinnedPieceCannotExposeKing() {
        // White bishop e2 (6,4) pinned by black rook e8 (0,4) to white king e1 (7,4)
        val s = ChessState(
            boardOf(
                wk(7, 4), bk(0, 0),
                Triple(6, 4, Piece(PieceType.Bishop, PieceColor.White)),
                Triple(0, 4, Piece(PieceType.Rook, PieceColor.Black))
            ),
            toMove = PieceColor.White
        )
        assertTrue(legalMoves(s).none { it.from == (6 to 4) })
    }

    @Test fun foolsMateIsCheckmate() {
        var s = initialChessState()
        fun play(from: Pair<Int, Int>, to: Pair<Int, Int>) {
            val m = legalMoves(s).first { it.from == from && it.to == to }
            s = applyChessMove(s, m)
        }
        play(6 to 5, 5 to 5) // f3
        play(1 to 4, 3 to 4) // e5
        play(6 to 6, 4 to 6) // g4
        play(0 to 3, 4 to 7) // Qh4#
        assertEquals(ChessStatus.Checkmate, chessStatus(s))
    }

    @Test fun backRankCheckmateDetected() {
        val s = ChessState(
            boardOf(
                wk(7, 7), bk(0, 0),
                Triple(6, 6, Piece(PieceType.Pawn, PieceColor.White)),
                Triple(6, 7, Piece(PieceType.Pawn, PieceColor.White)),
                Triple(7, 0, Piece(PieceType.Rook, PieceColor.Black))
            ),
            toMove = PieceColor.White
        )
        assertEquals(ChessStatus.Checkmate, chessStatus(s))
    }

    @Test fun stalemateDetected() {
        val s = ChessState(
            boardOf(
                wk(7, 0), bk(5, 0),
                Triple(6, 1, Piece(PieceType.Rook, PieceColor.Black))
            ),
            toMove = PieceColor.White
        )
        assertEquals(ChessStatus.Stalemate, chessStatus(s))
    }

    @Test fun promotionCreatesQueen() {
        val s = ChessState(
            boardOf(wk(7, 4), bk(0, 0), Triple(1, 0, Piece(PieceType.Pawn, PieceColor.White))),
            toMove = PieceColor.White
        )
        // Push the pawn straight to the last rank — it should auto-promote.
        val push = legalMoves(s).first { it.from == (1 to 0) && it.to == (0 to 0) }
        val next = applyChessMove(s, push)
        assertEquals(PieceType.Queen, next.board[0][0]?.type)
    }

    @Test fun botReturnsLegalMove() {
        val s = initialChessState()
        val move = chessBestMove(s, depth = 2)
        assertNotNull(move)
        assertTrue(legalMoves(s).any { it.from == move!!.from && it.to == move.to })
    }
}
