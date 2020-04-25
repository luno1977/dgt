package de.luno1977.dgt.livechess;

import de.luno1977.dgt.livechess.WebSocketFeed.Base;
import de.luno1977.dgt.livechess.WebSocketFeed.EBoardEvent;
import de.luno1977.dgt.livechess.model.EBoardEventResponse;
import de.luno1977.dgt.livechess.model.EBoardResponse;
import de.luno1977.dgt.livechess.model.EBoardResponse.EBoardRef;

public class EBoardEventFeed extends Base<EBoardEvent, EBoardEventResponse, EBoardRef> {

    /**
     * @param serialNr the serialNr of the eBoard.
     */
    public EBoardEventFeed(String serialNr) {
        super("eboardevent", new EBoardRef(serialNr), EBoardEvent.class);
    }

    public void flip(boolean flipped) {
        new WebSocketCall.Flip.Handler(flipped).call();
    }

    public void setup(String fen) {
        new WebSocketCall.Setup.Handler(fen).call();
    }
}
