package de.luno1977.dgt.livechess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FlipParam {
    private final boolean flip;

    @JsonCreator
    public FlipParam(@JsonProperty("flip") boolean flip) {
        this.flip = flip;
    }

    @JsonProperty("fen")
    public boolean getFlip() {
        return flip;
    }
}
