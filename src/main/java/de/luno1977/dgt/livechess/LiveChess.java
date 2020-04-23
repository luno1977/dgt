package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.luno1977.dgt.livechess.WebSocketCall.EBoards;
import de.luno1977.dgt.livechess.WebSocketCall.Sources;
import de.luno1977.dgt.livechess.WebSocketResponse.EBoardsResponse;
import de.luno1977.dgt.livechess.WebSocketResponse.SourcesResponse;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.*;

/**
 *
 */
public class LiveChess {

    private static LiveChess instance;
    private final Connector connector;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private LiveChess() {
        URI wsEndpoint = LiveChessConfig.getInstance().getLiveChessWebSocketEndpoint();
        try {
            connector = new Connector(wsEndpoint);
        } catch (IOException | DeploymentException e) {
            throw new LiveChessException("Could not connect to LiveChess under: " + wsEndpoint, e);
        }
    }

    public static LiveChess getInstance() {
        if (instance == null) {
            instance = new LiveChess();
        }
        return instance;
    }

    public EBoardsResponse getEBoards() { return new EBoards.Handler().call(); }

    public SourcesResponse getSources() { return new Sources.Handler().call(); }

    public EBoardEventFeed subscribe() {
        EBoardEventFeed feed = new EBoardEventFeed();
        feed.subscribe();
        return feed;
    }

    public void unsubscribe(EBoardEventFeed feed) {
        feed.unsubscribe();
    }

    @ClientEndpoint
    protected static class Connector extends java.util.Observable {

        Session userSession = null;

        public Connector(URI endpointURI) throws IOException, DeploymentException {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        }

        /**
         * Callback hook for Connection open events.
         *
         * @param userSession the userSession which is opened.
         */
        @OnOpen
        public void onOpen(Session userSession) {
            this.userSession = userSession;
        }

        /**
         * Callback hook for Connection close events.
         *
         * @param userSession the userSession which is getting closed.
         * @param reason the reason for connection close
         */
        @OnClose
        public void onClose(Session userSession, CloseReason reason) {
            this.userSession = null;
        }

        /**
         * Callback hook for Message Events. This method will be invoked when a client send a message.
         *
         * @param message The text message
         */
        @OnMessage
        public void onMessage(String message) {
            this.setChanged();
            this.notifyObservers(message);
        }

        public void sendMessage(String message) {
            this.userSession.getAsyncRemote().sendText(message);
        }
    }

    public abstract static class CallHandler<I extends WebSocketCall<?>, R extends WebSocketResponse<?>>
            implements Observer {

        protected final ObjectMapper mapper = new ObjectMapper();
        private final I input;
        private final Class<R> responseClass;
        private R result;
        private boolean stopped = false;

        protected CallHandler(I input, Class<R> responseClass) {
            this.input = input;
            this.responseClass = responseClass;
        }

        protected R mapValue(String jsonResponse) throws JsonProcessingException {
            return mapper.readValue(jsonResponse, responseClass);
        }

        public R call() {
            try {
                ExecutorService executor = getInstance().executor;
                executor.submit(getCaller()).get(3, TimeUnit.SECONDS);
                return result;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                return null;
            }
        }

        private Runnable getCaller() {
            return () -> {
                Connector connector = getInstance().connector;
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
            };
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
                throw new IllegalStateException(e);
            }
        }

        public void stop() {
            stopped = true;
        }
    }
}
