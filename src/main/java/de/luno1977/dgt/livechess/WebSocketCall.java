package de.luno1977.dgt.livechess;

import de.luno1977.dgt.livechess.LiveChess.CallHandler;
import de.luno1977.dgt.livechess.WebSocketFeed.FeedRef;
import de.luno1977.dgt.livechess.WebSocketResponse.SourcesResponse;

import javax.annotation.Nullable;

import static de.luno1977.dgt.livechess.WebSocketResponse.Ack;
import static de.luno1977.dgt.livechess.WebSocketResponse.EBoardsResponse;

public interface WebSocketCall<P> extends WebSocketCommunication<P> {

    String getCall();

    class EBoards extends BaseCall<Object> /* implements WebSocketCall<Object> */ {
        public static class Handler extends CallHandler<EBoards, EBoardsResponse> {
            public Handler() { super(new EBoards(), EBoardsResponse.class); }
        }
    }

    class Sources extends BaseCall<Object> /* implements WebSocketCall<Object> */ {
        public static class Handler extends CallHandler<Sources, SourcesResponse> {
            public Handler() { super(new Sources(), SourcesResponse.class); }
        }
    }

    class Subscribe extends BaseCall<WebSocketFeed<?,?,?>> {
        public Subscribe(WebSocketFeed<?,?,?> feed) { super(feed); }

        public static class Handler extends CallHandler<Subscribe, Ack> {
            public Handler(WebSocketFeed<?,?,?> feed) { super(new Subscribe(feed), Ack.class); }
        }
    }

    class Unsubscribe extends BaseCall<FeedRef> {
        public Unsubscribe(WebSocketFeed<?,?,?> feed) { super(new FeedRef(feed.getId())); }

        public static class Handler extends CallHandler<Unsubscribe, Ack> {
            public Handler(WebSocketFeed<?,?,?> feed) { super(new Unsubscribe(feed), Ack.class); }
        }
    }


    /** Fixme parameters
    class Flip extends BaseCall<Boolean> {
        public Flip(boolean flipped) { super(flipped); }

        public static class Handler extends CallHandler<Flip, Ack> {
            public Handler(boolean flipped) { super(new Flip(flipped), Ack.class); }
        }
    }

    class Setup extends BaseCall<String> {
        public Setup(String fen) { super(fen); }

        public static class Handler extends CallHandler<Setup, Ack> {
            public Handler(String fen) { super(new Setup(fen), Ack.class); }
        }
    }
    **/ //Fixme

    class BaseCall<P> implements WebSocketCall<P> {

        private final long id;
        @Nullable private final P param;

        public BaseCall(@Nullable P param) {
            this.id = IDS.getAndIncrement();
            this.param = param;
        }

        public BaseCall() {
            this(null);
        }

        @Override
        public String getCall() {
            return getClass().getSimpleName().toLowerCase();
        }

        @Override
        public long getId() {
            return id;
        }

        @Nullable @Override
        public P getParam() {
            return param;
        }
    }
}
