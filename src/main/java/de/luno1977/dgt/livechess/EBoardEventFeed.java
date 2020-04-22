package de.luno1977.dgt.livechess;

import de.luno1977.dgt.livechess.model.EBoardEvent;
import de.luno1977.dgt.livechess.model.EBoardEventResponse;
import io.reactivex.Observable;

public class EBoardEventFeed extends WebSocketFeed.BaseFeed<EBoardEvent, EBoardEventResponse> {

    private long id;
    private Boolean flipped;
    private boolean subscribed;

    public EBoardEventFeed() {
        id = FEED_IDS.getAndIncrement();
        subscribed = false;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getFeed() {
        return null;
    }

    @Override
    public Observable<EBoardEvent> events() {
        return null;
    }

    @Override
    public boolean isSubscribed() {
        return subscribed;
    }

    public void flip() {

    }

    public void setup() {

    }

}
