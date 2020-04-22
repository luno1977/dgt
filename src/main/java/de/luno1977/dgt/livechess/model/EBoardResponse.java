package de.luno1977.dgt.livechess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.luno1977.dgt.livechess.model.ClockResponse;

public class EBoardResponse {

    private final String serialNr;
    private final String source;
    private final String state;
    private final String battery;
    private final String comment;
    private final String board; //class Board ?!
    private final boolean flipped;
    private final ClockResponse clock;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EBoardResponse(
             @JsonProperty("serialnr") String serialNr,
             @JsonProperty("source") String source,
             @JsonProperty("state") String state,
             @JsonProperty("battery") String battery,
             @JsonProperty("comment") String comment,
             @JsonProperty("board") String board,
             @JsonProperty("flipped") boolean flipped,
             @JsonProperty("clock") ClockResponse clock) {
        this.serialNr = serialNr;
        this.source = source;
        this.state = state;
        this.battery = battery;
        this.comment = comment;
        this.board = board;
        this.flipped = flipped;
        this.clock = clock;
    }

    public String getSerialNr() {
        return serialNr;
    }

    public String getSource() {
        return source;
    }

    public String getState() {
        return state;
    }

    public String getBattery() {
        return battery;
    }

    public String getComment() {
        return comment;
    }

    public String getBoard() {
        return board;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public ClockResponse getClock() {
        return clock;
    }

    @Override
    public String toString() {
        return "EBoardResponse{" +
                "serialNr='" + serialNr + '\'' +
                ", source='" + source + '\'' +
                ", state='" + state + '\'' +
                ", battery='" + battery + '\'' +
                ", comment='" + comment + '\'' +
                ", board='" + board + '\'' +
                ", flipped=" + flipped +
                ", clock=" + clock +
                '}';
    }
}
