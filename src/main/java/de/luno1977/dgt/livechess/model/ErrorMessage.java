package de.luno1977.dgt.livechess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorMessage {
    private final String message;
    private final String stacktrace;

    @JsonCreator
    public ErrorMessage(
            @JsonProperty("message") String message,
            @JsonProperty("stacktrace") String stacktrace) {
        this.message = message;
        this.stacktrace = stacktrace;
    }

    public String getMessage() {
        return message;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "message='" + message + '\'' +
                ", stacktrace='" + stacktrace + '\'' +
                '}';
    }
}
