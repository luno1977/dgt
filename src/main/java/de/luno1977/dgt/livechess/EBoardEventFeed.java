package de.luno1977.dgt.livechess;

import de.luno1977.dgt.livechess.WebSocketFeed.Base;
import de.luno1977.dgt.livechess.WebSocketFeed.EBoardEvent;
import de.luno1977.dgt.livechess.model.EBoardEventResponse;
import de.luno1977.dgt.livechess.model.EBoardResponse.EBoardRef;
import de.luno1977.dgt.livechess.model.FEN;
import de.luno1977.dgt.livechess.model.FlipParam;

public class EBoardEventFeed extends Base<EBoardEvent, EBoardEventResponse, EBoardRef> {

    /**
     * @param serialNr the serialNr of the eBoard.
     */
    public EBoardEventFeed(String serialNr) {
        super("eboardevent", new EBoardRef(serialNr), EBoardEvent.class);
    }

    public void flip(boolean flipped) {
        new Flip.Handler(this.getId(), flipped).call();
    }

    public void setup(FEN fen) { new Setup.Handler(this.getId(), fen).call();
    }

    private static class Setup extends WebSocketFeed.FeedCall<FEN> {
        Setup(long feedId, FEN fen) { super(feedId, fen); }

        private static class Handler extends LiveChess.CallHandler<EBoardEventFeed.Setup, WebSocketResponse.Ack> {
            public Handler(long feedId, FEN fen) { super(new Setup(feedId, fen), WebSocketResponse.Ack.class); }
        }
    }

    private static class Flip extends WebSocketFeed.FeedCall<FlipParam> {
        Flip(long feedId, FlipParam flip) { super(feedId, flip); }

        private static class Handler extends LiveChess.CallHandler<EBoardEventFeed.Flip, WebSocketResponse.Ack> {
            public Handler(long feedId, boolean flip) {
                super(new Flip(feedId, new FlipParam(flip)), WebSocketResponse.Ack.class); }
        }
    }
}
