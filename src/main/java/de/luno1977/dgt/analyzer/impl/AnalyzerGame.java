package de.luno1977.dgt.analyzer.impl;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveList;
import javafx.geometry.Pos;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AnalyzerGame implements Exportable {

    private static class DetailedMove extends Move {
        final boolean isLegal;

        public DetailedMove(Move m, boolean isLegal) {
            super(m.getFrom(), m.getTo(), m.getPromotion());
            this.setSan(m.getSan());
            this.isLegal = isLegal;
        }

        @Override
        public String toString() {
            return (getFrom().name()+getTo().name()).toLowerCase() + ((isLegal) ? "" : " (illegal)");
        }
    }

    private final Notation notation;
    private Map<String, MoveList> followUpPositions;

    public AnalyzerGame(Position startPosition) {
        this.notation = new Notation(startPosition);
        updateFollower();
    }

    public AnalyzerGame() {
        this(new Position());
    }

    public boolean insertIfFollowUpPosition(String boardRep) {
        for (String followUp : followUpPositions.keySet()) {
            if (followUp.startsWith(boardRep)) {
                MoveList moves = followUpPositions.get(followUp);
                if (((DetailedMove)moves.getLast()).isLegal) {
                    if (moves.size() == 1) {
                        notation.applyMove(moves.getFirst(), new Position(followUp));
                    } else {
                        Board b = new Board();
                        b.loadFromFen(notation.getCurrentPosition().getFen());
                        for (Move m : moves) {
                            b.doMove(m);
                            notation.applyMove(m, new Position(b.getFen()));
                        }
                    }
                    updateFollower();
                    return true;

                }
            }
        }

        return false;

    }

    public boolean mustJumpToFormerPosition(String boardRep) {
        Position formerPosition = notation.findFirstByBoardRep(boardRep);
        if (formerPosition != null) {
            notation.jump(formerPosition);
            updateFollower();
            return true;
        }
        return false;
    }

    private void updateFollower() {
        followUpPositions = new HashMap<>();
        Position current = getNotation().getCurrentPosition();
        updateFollower(current.getFen(), new MoveList(current.getFen()));
    }

    private void updateFollower(String fromFen, MoveList moves) {
        Board board = new Board();
        board.loadFromFen(fromFen);
        System.out.println("Follow Ups of: " + fromFen);

        MoveList pseudoLegalMoves = MoveGenerator.generatePseudoLegalMoves(board);

        for (Move m : pseudoLegalMoves) {
            addFollowUpMove(moves, board, m, board.isMoveLegal(m, true));
        }
    }

    private void addFollowUpMove(MoveList moves, Board board, Move m, boolean legal) {
        DetailedMove detailedMove = new DetailedMove(m, legal);
        MoveList followUpMoveList = new MoveList(moves);
        followUpMoveList.add(detailedMove);

        if (legal) {
            board.doMove(m);

            String followUpPosition = board.getFen();
            followUpPositions.put(followUpPosition, new MoveList(followUpMoveList));

            System.out.println("FollowUp: " + followUpPosition + " via Moves: " + followUpMoveList);

            if (followUpMoveList.size() < 2) {
                updateFollower(board.getFen(), followUpMoveList);
            }

            board.undoMove();
        } else {
            Board illegalBoard = new Board();
            illegalBoard.loadFromFen(board.getFen());
            Piece piece = illegalBoard.getPiece(detailedMove.getFrom());
            illegalBoard.unsetPiece(piece, detailedMove.getFrom());
            illegalBoard.setPiece(piece, detailedMove.getTo());

            String followUpPosition = illegalBoard.getFen(); //invalid fen!
            followUpPositions.put(followUpPosition, new MoveList(followUpMoveList));

            System.out.println("FollowUp: " + followUpPosition + " via Moves: " + followUpMoveList);

        }
    }

    @Override
    public void export(ExportFormat exportFormat, OutputStream viaStream) throws IOException {
        viaStream.close();
    }

    public Notation getNotation() {
        return notation;
    }

}
