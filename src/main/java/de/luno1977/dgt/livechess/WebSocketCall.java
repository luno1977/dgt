package de.luno1977.dgt.livechess;

public interface WebSocketCall<P> extends WebSocketCommunication<P> {
    String getCall();
}
