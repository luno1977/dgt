package de.luno1977.dgt.analyzer.impl;

import java.io.IOException;
import java.io.OutputStream;

public interface Exportable {
    enum ExportFormat {
        PGN, HTML, PDF, MS_WORD, ODF
    }

    void export(ExportFormat exportFormat, OutputStream viaStream) throws IOException;
}
