package de.luno1977.dgt.livechess;

public class LiveChessException extends RuntimeException {
    public LiveChessException(String msg, Exception e) {
        super(msg, e);
    }
}
