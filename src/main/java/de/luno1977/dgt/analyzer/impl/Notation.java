package de.luno1977.dgt.analyzer.impl;

import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Notation {

    private final Position startPosition;
    private Position currentPosition;
    private NavigableMap<Position, List<Move>> lines = new TreeMap<>();

    public Notation(Position startPosition) {
        this.startPosition = startPosition;
        this.currentPosition = startPosition;
        lines.put(startPosition, new ArrayList<>());
    }

    /**
     * Adds a move to the current position and updates the current position to
     * the resultingPosition. No validation here, input values must be correct
     * in regards of the current position. A move is not added twice into the
     * notation.
     *
     * @param move the move to apply to the current position
     * @param resultingPosition the position which is valid after the move was made.
     */
    public void applyMove(Move move, Position resultingPosition) {
        List<Move> moveList = lines.get(currentPosition);
        if (!moveList.contains(move)) {
            moveList.add(move);
            lines.put(resultingPosition, new ArrayList<>());
        }
        this.currentPosition = resultingPosition;
    }

    public void back() {
        this.currentPosition = lines.lowerKey(currentPosition);
    }

    public void jump(Position position) {
        if (lines.containsKey(position)) {
            this.currentPosition = position;
        } else {
            throw new IllegalStateException("Position not in notation " + position);
        }
    }

    public Position findFirstByBoardRep(String boardRep) {
        for (Position p : lines.navigableKeySet()) {
            if (p.getFen().startsWith(boardRep)) {
                return p;
            }
        }

        return null;
    }


    public Position getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Notation:\n");
        out.append("\tStartAt="+startPosition.getFen() + "\n");
        out.append("\tCurrent="+currentPosition.getFen() + "\n");
        for (Position p : lines.navigableKeySet()) {
            String marker = "";
            if (p.equals(currentPosition)) {
                marker = ">>>";
            }
            out.append("\t\tPosition: "+ marker + p.getFen() + " contains follow up moves: " + lines.get(p) + "\n");
        }

        return out.toString();
    }
}
