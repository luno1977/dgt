package de.luno1977.dgt.livechess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FEN {
    private final String fen;

    @JsonCreator
    public FEN(@JsonProperty("fen") String fen) {
        this.fen = fen;
    }

    @JsonProperty("fen")
    public String getFen() {
        return fen;
    }
}
