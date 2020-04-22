package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicLong;

public interface WebSocketCall<P> extends WebSocketCommunication<P> {
    AtomicLong CALL_IDS = new AtomicLong(0L);

    String getCall();

    class EBoards extends BaseCall<Object> /* implements WebSocketCall<Object> */ {
        public EBoards() { super("eBoards".toLowerCase(), null); }

        public static class Handler extends CallHandler<EBoards, WebSocketResponse.EBoardsResponse> {
            protected Handler(LiveChess.Connector connector) {
                super(connector, new EBoards(), WebSocketResponse.EBoardsResponse.class);
            }
        }
    }

    class Sources extends BaseCall<Object> /* implements WebSocketCall<Object> */ {
        public Sources() { super("sources", null); }

        public static class Handler extends CallHandler<Sources, WebSocketResponse.SourcesResponse> {
            protected Handler(LiveChess.Connector connector) {
                super(connector, new Sources(), WebSocketResponse.SourcesResponse.class);
            }
        }
    }

    class Subscribe extends BaseCall<WebSocketFeed<?,?>> {
        public Subscribe(WebSocketFeed<?,?> param) { super("subscribe", param); }

        public static class Handler extends CallHandler<Subscribe, WebSocketResponse.Ack> {
            protected Handler(LiveChess.Connector connector, WebSocketFeed<?,?> param) {
                super(connector, new Subscribe(param), WebSocketResponse.Ack.class);
            }
        }
    }

    class Unsubscribe extends BaseCall<Long> {
        public Unsubscribe(WebSocketFeed<?,?> feed) { super("unsubscribe", feed.getId()); }

        public static class Handler extends CallHandler<Unsubscribe, WebSocketResponse.Ack> {
            protected Handler(LiveChess.Connector connector, WebSocketFeed<?,?> feed) {
                super(connector, new Unsubscribe(feed), WebSocketResponse.Ack.class);
            }
        }
    }

    class BaseCall<P> implements WebSocketCall<P> {

        private final long id;
        private final String call;
        @Nullable private final P param;

        public BaseCall(String call, @Nullable P param) {
            this.call = call;
            this.id = CALL_IDS.getAndIncrement();
            this.param = param;
        }

        @Override
        public String getCall() {
            return call;
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

    abstract class CallHandler<I extends WebSocketCall<?>, R extends WebSocketResponse<?>>
            implements Runnable, Observer {

        protected final ObjectMapper mapper = new ObjectMapper();
        private final LiveChess.Connector connector;
        private final I input;
        private final Class<R> responseClass;
        private R result;
        private boolean stopped = false;

        protected CallHandler(LiveChess.Connector connector, I input, Class<R> responseClass) {
            this.connector = connector;
            this.input = input;
            this.responseClass = responseClass;
        }

        protected R mapValue(String jsonResponse) throws JsonProcessingException {
            return mapper.readValue(jsonResponse, responseClass);
        }

        @Override
        public void run() {
            try {
                connector.addObserver(this);
                String message = mapper.writeValueAsString(input);
                connector.sendMessage(message);

                synchronized (this) {
                    while (result == null && !stopped) {
                        try {
                            this.wait(200);
                        } catch (InterruptedException e) {
                            System.out.println("Interrupted: " + stopped + ", " + result);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connector.deleteObserver(this);
            }
        }

        @Override
        public void update(java.util.Observable o, Object msg) {
            String message = msg.toString();
            try {
                Map<String, Object> stringObjectMap = mapper.readValue(message,
                        new TypeReference<Map<String, Object>>() {});
                long messageId = ((Number) stringObjectMap.get("id")).longValue();

                if (messageId == input.getId()) {
                    result = mapValue(message);
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }

        public R getResult() {
            return result;
        }

        public void stop() {
            stopped = true;
        }
    }
}
