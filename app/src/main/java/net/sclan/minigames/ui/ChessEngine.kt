package net.sclan.minigames.ui

// ---------------------------------------------------------------------------
// Pure chess engine — full legal moves incl. castling, en passant, promotion,
// check / checkmate / stalemate detection, and an alpha-beta (negamax) bot.
// Board rows: 0 = top (Black back rank), 7 = bottom (White back rank).
// White moves up (decreasing row). Columns 0..7 = files a..h.
// ---------------------------------------------------------------------------

enum class PieceColor { White, Black } // human is White (bottom)

enum class PieceType(val value: Int) {
    Pawn(100), Knight(320), Bishop(330), Rook(500), Queen(900), King(20000)
}

data class Piece(val type: PieceType, val color: PieceColor)

typealias ChessBoard = List<List<Piece?>>

data class ChessMove(
    val from: Pair<Int, Int>,
    val to: Pair<Int, Int>,
    val promotion: PieceType? = null,
    val isEnPassant: Boolean = false,
    val isCastle: Boolean = false
)

data class ChessState(
    val board: ChessBoard,
    val toMove: PieceColor,
    val castling: Set<String> = setOf("WK", "WQ", "BK", "BQ"),
    val enPassant: Pair<Int, Int>? = null
)

enum class ChessStatus { Ongoing, Check, Checkmate, Stalemate }

private fun backRank(color: PieceColor) = listOf(
    PieceType.Rook, PieceType.Knight, PieceType.Bishop, PieceType.Queen,
    PieceType.King, PieceType.Bishop, PieceType.Knight, PieceType.Rook
).map { Piece(it, color) }

fun initialChessState(): ChessState {
    val board = MutableList(8) { arrayOfNulls<Piece>(8).toMutableList() }
    board[0] = backRank(PieceColor.Black).toMutableList()
    board[1] = MutableList(8) { Piece(PieceType.Pawn, PieceColor.Black) }
    board[6] = MutableList(8) { Piece(PieceType.Pawn, PieceColor.White) }
    board[7] = backRank(PieceColor.White).toMutableList()
    return ChessState(board.map { it.toList() }, PieceColor.White)
}

private fun opposite(c: PieceColor) = if (c == PieceColor.White) PieceColor.Black else PieceColor.White
private fun inBounds(r: Int, c: Int) = r in 0..7 && c in 0..7

/** Does any [color] piece attack (r,c)? Used for check detection. */
fun squareAttacked(board: ChessBoard, r: Int, c: Int, color: PieceColor): Boolean {
    // Pawns (attack "forward" for their color)
    val pawnDir = if (color == PieceColor.White) 1 else -1 // white attacks upward from below → its pawns sit at r+1
    for (dc in intArrayOf(-1, 1)) {
        val pr = r + pawnDir
        val pc = c + dc
        if (inBounds(pr, pc)) {
            val p = board[pr][pc]
            if (p != null && p.color == color && p.type == PieceType.Pawn) return true
        }
    }
    // Knights
    val knightJumps = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
    for ((dr, dc) in knightJumps) {
        val nr = r + dr; val nc = c + dc
        if (inBounds(nr, nc)) {
            val p = board[nr][nc]
            if (p != null && p.color == color && p.type == PieceType.Knight) return true
        }
    }
    // King (adjacent)
    for (dr in -1..1) for (dc in -1..1) {
        if (dr == 0 && dc == 0) continue
        val nr = r + dr; val nc = c + dc
        if (inBounds(nr, nc)) {
            val p = board[nr][nc]
            if (p != null && p.color == color && p.type == PieceType.King) return true
        }
    }
    // Sliding: rook/queen (orthogonal), bishop/queen (diagonal)
    val ortho = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    val diag = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
    for ((dr, dc) in ortho) {
        var nr = r + dr; var nc = c + dc
        while (inBounds(nr, nc)) {
            val p = board[nr][nc]
            if (p != null) {
                if (p.color == color && (p.type == PieceType.Rook || p.type == PieceType.Queen)) return true
                break
            }
            nr += dr; nc += dc
        }
    }
    for ((dr, dc) in diag) {
        var nr = r + dr; var nc = c + dc
        while (inBounds(nr, nc)) {
            val p = board[nr][nc]
            if (p != null) {
                if (p.color == color && (p.type == PieceType.Bishop || p.type == PieceType.Queen)) return true
                break
            }
            nr += dr; nc += dc
        }
    }
    return false
}

private fun findKing(board: ChessBoard, color: PieceColor): Pair<Int, Int>? {
    for (r in 0..7) for (c in 0..7) {
        val p = board[r][c]
        if (p != null && p.color == color && p.type == PieceType.King) return r to c
    }
    return null
}

fun isInCheck(state: ChessState, color: PieceColor): Boolean {
    val king = findKing(state.board, color) ?: return false
    return squareAttacked(state.board, king.first, king.second, opposite(color))
}

/** Pseudo-legal moves for the piece at (r,c) — ignores leaving own king in check. */
private fun pseudoMoves(state: ChessState, r: Int, c: Int): List<ChessMove> {
    val board = state.board
    val piece = board[r][c] ?: return emptyList()
    val color = piece.color
    val moves = mutableListOf<ChessMove>()
    val here = r to c

    fun addSliding(dirs: List<Pair<Int, Int>>) {
        for ((dr, dc) in dirs) {
            var nr = r + dr; var nc = c + dc
            while (inBounds(nr, nc)) {
                val t = board[nr][nc]
                if (t == null) moves.add(ChessMove(here, nr to nc))
                else { if (t.color != color) moves.add(ChessMove(here, nr to nc)); break }
                nr += dr; nc += dc
            }
        }
    }

    when (piece.type) {
        PieceType.Pawn -> {
            val dir = if (color == PieceColor.White) -1 else 1
            val startRow = if (color == PieceColor.White) 6 else 1
            val promoRow = if (color == PieceColor.White) 0 else 7
            val one = r + dir
            if (inBounds(one, c) && board[one][c] == null) {
                if (one == promoRow) moves.add(ChessMove(here, one to c, promotion = PieceType.Queen))
                else moves.add(ChessMove(here, one to c))
                val two = r + 2 * dir
                if (r == startRow && board[two][c] == null) moves.add(ChessMove(here, two to c))
            }
            for (dc in intArrayOf(-1, 1)) {
                val nr = r + dir; val nc = c + dc
                if (!inBounds(nr, nc)) continue
                val t = board[nr][nc]
                if (t != null && t.color != color) {
                    if (nr == promoRow) moves.add(ChessMove(here, nr to nc, promotion = PieceType.Queen))
                    else moves.add(ChessMove(here, nr to nc))
                } else if (t == null && state.enPassant == (nr to nc)) {
                    moves.add(ChessMove(here, nr to nc, isEnPassant = true))
                }
            }
        }
        PieceType.Knight -> {
            for ((dr, dc) in listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)) {
                val nr = r + dr; val nc = c + dc
                if (inBounds(nr, nc)) {
                    val t = board[nr][nc]
                    if (t == null || t.color != color) moves.add(ChessMove(here, nr to nc))
                }
            }
        }
        PieceType.Bishop -> addSliding(listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1))
        PieceType.Rook -> addSliding(listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1))
        PieceType.Queen -> addSliding(listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1))
        PieceType.King -> {
            for (dr in -1..1) for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr; val nc = c + dc
                if (inBounds(nr, nc)) {
                    val t = board[nr][nc]
                    if (t == null || t.color != color) moves.add(ChessMove(here, nr to nc))
                }
            }
            // Castling
            val homeRow = if (color == PieceColor.White) 7 else 0
            if (r == homeRow && c == 4 && !isInCheck(state, color)) {
                val kSide = if (color == PieceColor.White) "WK" else "BK"
                val qSide = if (color == PieceColor.White) "WQ" else "BQ"
                val opp = opposite(color)
                if (kSide in state.castling &&
                    board[homeRow][5] == null && board[homeRow][6] == null &&
                    board[homeRow][7]?.type == PieceType.Rook &&
                    !squareAttacked(board, homeRow, 5, opp) && !squareAttacked(board, homeRow, 6, opp)
                ) {
                    moves.add(ChessMove(here, homeRow to 6, isCastle = true))
                }
                if (qSide in state.castling &&
                    board[homeRow][1] == null && board[homeRow][2] == null && board[homeRow][3] == null &&
                    board[homeRow][0]?.type == PieceType.Rook &&
                    !squareAttacked(board, homeRow, 3, opp) && !squareAttacked(board, homeRow, 2, opp)
                ) {
                    moves.add(ChessMove(here, homeRow to 2, isCastle = true))
                }
            }
        }
    }
    return moves
}

fun applyChessMove(state: ChessState, move: ChessMove): ChessState {
    val board = state.board.map { it.toMutableList() }
    val (fr, fc) = move.from
    val (tr, tc) = move.to
    val piece = board[fr][fc]!!
    val color = piece.color

    // En passant capture removes the pawn beside the destination
    if (move.isEnPassant) board[fr][tc] = null

    // Move the piece (promotion swaps type)
    board[fr][fc] = null
    board[tr][tc] = if (move.promotion != null) Piece(move.promotion, color) else piece

    // Castling moves the rook too
    if (move.isCastle) {
        val homeRow = tr
        if (tc == 6) { board[homeRow][5] = board[homeRow][7]; board[homeRow][7] = null }
        else if (tc == 2) { board[homeRow][3] = board[homeRow][0]; board[homeRow][0] = null }
    }

    // Update castling rights
    val castling = state.castling.toMutableSet()
    if (piece.type == PieceType.King) {
        if (color == PieceColor.White) { castling.remove("WK"); castling.remove("WQ") }
        else { castling.remove("BK"); castling.remove("BQ") }
    }
    fun touchRook(r: Int, c: Int) {
        when (r to c) {
            7 to 7 -> castling.remove("WK"); 7 to 0 -> castling.remove("WQ")
            0 to 7 -> castling.remove("BK"); 0 to 0 -> castling.remove("BQ")
        }
    }
    touchRook(fr, fc) // rook moved from home
    touchRook(tr, tc) // rook captured on home

    // New en-passant target if a pawn advanced two
    val enPassant = if (piece.type == PieceType.Pawn && kotlin.math.abs(tr - fr) == 2) {
        ((fr + tr) / 2) to fc
    } else null

    return ChessState(board.map { it.toList() }, opposite(color), castling, enPassant)
}

fun legalMoves(state: ChessState): List<ChessMove> {
    val result = mutableListOf<ChessMove>()
    for (r in 0..7) for (c in 0..7) {
        val p = state.board[r][c] ?: continue
        if (p.color != state.toMove) continue
        for (m in pseudoMoves(state, r, c)) {
            val next = applyChessMove(state, m)
            if (!isInCheck(next.copy(toMove = state.toMove), state.toMove)) result.add(m)
        }
    }
    return result
}

fun chessStatus(state: ChessState): ChessStatus {
    val moves = legalMoves(state)
    val check = isInCheck(state, state.toMove)
    return when {
        moves.isEmpty() && check -> ChessStatus.Checkmate
        moves.isEmpty() -> ChessStatus.Stalemate
        check -> ChessStatus.Check
        else -> ChessStatus.Ongoing
    }
}

// ---------------------------------------------------------------------------
// Bot — negamax with alpha-beta, material + light central bonus.
// ---------------------------------------------------------------------------

private const val MATE = 1_000_000

private fun materialFor(board: ChessBoard, color: PieceColor): Int {
    var score = 0
    for (r in 0..7) for (c in 0..7) {
        val p = board[r][c] ?: continue
        val v = p.type.value
        // Small nudge toward the center
        val central = 3 - (kotlin.math.abs(3.5 - r) + kotlin.math.abs(3.5 - c)).toInt()
        val bonus = if (p.type == PieceType.King) 0 else central
        score += if (p.color == color) v + bonus else -(v + bonus)
    }
    return score
}

private fun orderMoves(state: ChessState, moves: List<ChessMove>): List<ChessMove> =
    moves.sortedByDescending { m -> state.board[m.to.first][m.to.second]?.type?.value ?: 0 }

private fun negamax(state: ChessState, depth: Int, alphaIn: Int, beta: Int): Int {
    val moves = legalMoves(state)
    if (moves.isEmpty()) {
        return if (isInCheck(state, state.toMove)) -MATE - depth else 0
    }
    if (depth == 0) return materialFor(state.board, state.toMove)
    var alpha = alphaIn
    var best = Int.MIN_VALUE
    for (m in orderMoves(state, moves)) {
        val score = -negamax(applyChessMove(state, m), depth - 1, -beta, -alpha)
        if (score > best) best = score
        if (best > alpha) alpha = best
        if (alpha >= beta) break
    }
    return best
}

/** Picks the bot's best move at the given [depth], or null if no moves. */
fun chessBestMove(state: ChessState, depth: Int = 3): ChessMove? {
    val moves = legalMoves(state)
    if (moves.isEmpty()) return null
    var best = moves.first()
    var bestScore = Int.MIN_VALUE
    var alpha = -MATE * 2
    val beta = MATE * 2
    for (m in orderMoves(state, moves)) {
        val score = -negamax(applyChessMove(state, m), depth - 1, -beta, -alpha)
        if (score > bestScore) { bestScore = score; best = m }
        if (score > alpha) alpha = score
    }
    return best
}
