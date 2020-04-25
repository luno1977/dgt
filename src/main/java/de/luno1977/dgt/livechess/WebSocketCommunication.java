package de.luno1977.dgt.livechess;

public interface WebSocketCommunication<P> {

    /**
     * @return the id of the communication.
     *         {@link WebSocketFeed} ids are separated from {@link WebSocketCall} ids.
     */
    long getId();

    /**
     * @return the parameter(s) of the communication.
     */
    P getParam();
}
