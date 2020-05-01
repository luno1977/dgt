package de.luno1977.dgt.analyzer.impl;

import com.github.bhlangonijr.chesslib.Board;

import java.io.IOException;
import java.io.OutputStream;

public class AnalyzerGame implements Exportable {

    private final Notation notation;
    private final Board moveGenerator;

    public AnalyzerGame(Position startPosition) {
        this.moveGenerator = new Board();
        moveGenerator.loadFromFen(startPosition.getFen());
        this.notation = new Notation(startPosition);
    }

    public AnalyzerGame() {
        this(new Position());
    }

    @Override
    public void export(ExportFormat exportFormat, OutputStream viaStream) throws IOException {
        viaStream.close();
    }
}
