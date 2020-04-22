package de.luno1977.dgt.livechess.model;

import de.luno1977.dgt.livechess.WebSocketFeed;

public class EBoardEvent implements WebSocketFeed.Event<EBoardEventResponse> {

    @Override
    public String getResponse() {
        return null;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public EBoardEventResponse getParam() {
        return null;
    }
}
