package de.luno1977.dgt.analyzer.impl;

import com.github.bhlangonijr.chesslib.Board;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

public class Position implements Comparable<Position> {
    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final String fen;

    public Position(String fen) {
        this.fen = fen;
    }

    public Position() {
        this.fen = START_FEN;
    }

    public String getFen() {
        return fen;
    }

    @Override
    public int compareTo(@Nonnull Position other) {
        Board my = new Board(); my.loadFromFen(fen);
        Board ot = new Board(); ot.loadFromFen(checkNotNull(other).fen);

        int moveCountCompare = Integer.compare(my.getMoveCounter(), ot.getMoveCounter());
        if (moveCountCompare == 0) {
            return my.getSideToMove().compareTo(ot.getSideToMove());
        } else {
            return moveCountCompare;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        return fen.equals(position.fen);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fen);
    }
}
