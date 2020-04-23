package de.luno1977.dgt.livechess;

import de.luno1977.dgt.livechess.model.EBoardEvent;
import de.luno1977.dgt.livechess.model.EBoardEventResponse;
import io.reactivex.Observable;

public class EBoardEventFeed extends WebSocketFeed.BaseFeed<EBoardEvent, EBoardEventResponse> {

    public EBoardEventFeed() {
        super("eBoardEventFeed".toLowerCase());
    }

    public void flip(boolean flipped) {
        new WebSocketCall.Flip.Handler(flipped).call();
    }

    public void setup(String fen) {
        new WebSocketCall.Setup.Handler(fen).call();
    }
}
