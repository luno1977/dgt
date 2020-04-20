package de.luno1977.dgt.livechess;

public interface WebSocketResponse<P> extends WebSocketCommunication<P> {
    String getResponse();
}
