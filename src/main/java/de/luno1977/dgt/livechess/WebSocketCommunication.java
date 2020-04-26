package de.luno1977.dgt.livechess;

import java.util.concurrent.atomic.AtomicLong;

public interface WebSocketCommunication<P> {

    AtomicLong IDS = new AtomicLong(0L);

    /**
     * @return the id of the communication.
     */
    long getId();

    /**
     * @return the parameter(s) of the communication.
     */
    P getParam();
}
