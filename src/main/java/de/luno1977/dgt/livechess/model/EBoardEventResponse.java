package de.luno1977.dgt.livechess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class EBoardEventResponse {

    private final String serialNr;
    private final boolean flipped;
    private final String board;
    private final ClockResponse clock;
    private final String start;
    private final String[] san;
    private final boolean match;

    public EBoardEventResponse(
            @JsonProperty("serialnr") String serialNr,
            @JsonProperty("flipped") boolean flipped,
            @JsonProperty("board") String board,
            @JsonProperty("clock") ClockResponse clock,
            @JsonProperty("start") String start,
            @JsonProperty("san") String[] san,
            @JsonProperty("match") boolean match) {
        this.serialNr = serialNr;
        this.flipped = flipped;
        this.board = board;
        this.clock = clock;
        this.start = start;
        this.san = san;
        this.match = match;
    }

    /**
     * @return string with the serialNr of the board generating an event.
     */
    public String getSerialNr() {
        return serialNr;
    }

    /**
     * @return indicator if the board is flipped or not
     */
    public boolean isFlipped() {
        return flipped;
    }

    /**
     * @return current position on the board, may be omitted if there was no board change
     */
    public String getBoard() {
        return board;
    }

    /**
     * @return value of the clock, may be omitted if there was no clock change
     */
    public ClockResponse getClock() {
        return clock;
    }

    /**
     * @return FEN string with the position from where reconstruction of moves is done
     */
    public String getStart() {
        return start;
    }

    /**
     * @return array of SAN values with the detected moves
     */
    public String[] getSan() {
        return san;
    }

    /**
     * @return boolean indicating if the current board exactly matches the reconstructed move
     */
    public boolean isMatch() {
        return match;
    }

    @Override
    public String toString() {
        return "EBoardEventResponse{" +
                "serialNr='" + serialNr + '\'' +
                ", flipped=" + flipped +
                ", board='" + board + '\'' +
                ", clock=" + clock +
                ", start='" + start + '\'' +
                ", san=" + Arrays.toString(san) +
                ", match=" + match +
                '}';
    }
}
