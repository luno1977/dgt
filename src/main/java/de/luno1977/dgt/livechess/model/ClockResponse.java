package de.luno1977.dgt.livechess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClockResponse {

    private final int white;
    private final int black;
    private final Boolean run;
    private final long time;

    @JsonCreator
    public ClockResponse(
            @JsonProperty("white") int white,
            @JsonProperty("black") int black,
            @JsonProperty("run") Boolean run,
            @JsonProperty("time") long time) {
        this.white = white;
        this.black = black;
        this.run = run;
        this.time = time;
    }

    /**
     * @returns number of seconds for white on the clock.
     */
    public int getWhite() {
        return white;
    }

    /**
     * @return number of seconds for black on the clock.
     */
    public int getBlack() {
        return black;
    }

    /**
     * @return null if the clock is not running, otherwise true for running clock for white,
     *         false when clock is running for black.
     */
    public Boolean getRun() {
        return run;
    }

    /**
     *
     * @retur timestamp in milliseconds when this information was retrieved
     *        from the clock. This allows for the calculation of the running clock value.
     */
    public long isTime() {
        return time;
    }

    @Override
    public String toString() {
        return "ClockResponse{" +
                "white=" + white +
                ", black=" + black +
                ", run=" + run +
                ", time=" + time +
                '}';
    }
}
