package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.luno1977.dgt.livechess.WebSocketCall.EBoards;
import de.luno1977.dgt.livechess.WebSocketCall.Sources;
import de.luno1977.dgt.livechess.WebSocketResponse.EBoardsResponse;
import de.luno1977.dgt.livechess.WebSocketResponse.ErrorResponse;
import de.luno1977.dgt.livechess.WebSocketResponse.SourcesResponse;
import de.luno1977.dgt.livechess.model.EBoardResponse;
import de.luno1977.dgt.livechess.model.ResponseHeader;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Observer;
import java.util.concurrent.*;

/**
 *
 */
public class LiveChess {

    private static LiveChess instance;
    private final Connector connector;
    private final ExecutorService callConnectionPool = Executors.newFixedThreadPool(5);
    private final ExecutorService feedConnectionPool = Executors.newCachedThreadPool();

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

    public EBoardEventFeed subscribe(EBoardResponse eBoard) {
        EBoardEventFeed feed = new EBoardEventFeed(eBoard.getSerialNr());
        feed.subscribe();
        return feed;
    }

    public void unsubscribe(EBoardEventFeed feed) {
        feed.unsubscribe();
    }


    @ClientEndpoint()
    protected static class Connector extends java.util.Observable {

        private Session userSession = null;

        public Connector(URI endpointURI) throws IOException, DeploymentException {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            try {
                Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        // Has a fixed 10 second timeout
                        container.connectToServer(this, endpointURI);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).get(400, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new IOException("Could not connect to LiveChess", e);
            }
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
            System.out.println("UserSession: " + userSession + " is closed with reason: " + reason);
            this.userSession = null;
        }

        /**
         * Callback hook for Message Events. This method will be invoked when a client send a message.
         *
         * @param message The text message
         */
        @OnMessage
        public void onMessage(String message) {
            System.out.println("Board response: " + message);
            this.setChanged();
            this.notifyObservers(message);
        }

        public void sendMessage(String message) {
            this.userSession.getAsyncRemote().sendText(message);
            System.out.println("Board message:" + message);
        }
    }

    public abstract static class CallHandler<I extends WebSocketCall<?>, R extends WebSocketResponse<?>>
            implements Observer {

        private final ObjectMapper mapper = new ObjectMapper();
        private final I input;
        private final Class<R> responseClass;
        private R result;
        private ErrorResponse error;
        private boolean stopped = false;

        protected CallHandler(I input, Class<R> responseClass) {
            this.input = input;
            this.responseClass = responseClass;
        }

        public R call() {
            try {
                ExecutorService executor = getInstance().callConnectionPool;
                executor.submit(getCaller()).get(3, TimeUnit.SECONDS);
                return result;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new LiveChessException("Error during WebSocketCall", e);
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
                        while (result == null && error == null & !stopped) {
                            try {
                                this.wait(200);
                            } catch (InterruptedException e) {
                                System.out.println("Interrupted: " + stopped + ", " + result);
                            }
                        }
                    }

                    if (error != null) {
                        throw new LiveChessException(error.getParam().getMessage(), error.getParam().getStacktrace());
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
                ResponseHeader header = mapper.readValue(message, ResponseHeader.class);
                if (header.getId() == input.getId()) {
                    if ("error".equals(header.getResponse())) {
                        error = mapper.readValue(message, ErrorResponse.class);
                    }
                    if ("call".equals(header.getResponse())) {
                        result = mapper.readValue(message, responseClass);
                    }
                    synchronized (this) { this.notifyAll(); }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }

        public void stop() {
            stopped = true;
        }
    }

    public static class FeedHandler<E extends WebSocketFeed.Event<P>, P> implements Observer {

        private final PublishSubject<E> publishSubject;
        private boolean stopped = false;
        private final ObjectMapper mapper = new ObjectMapper();
        private final ConcurrentLinkedQueue<E> events = new ConcurrentLinkedQueue<>();
        private final Class<E> eventsType;
        private final long feedId;

        public FeedHandler(long feedId, Class<E> eventsType) {
            this.publishSubject = PublishSubject.create();
            this.eventsType = eventsType;
            this.feedId = feedId;
        }

        public void start() {
            ExecutorService executor = getInstance().feedConnectionPool;
            executor.execute(getFeeder());
        }

        private Runnable getFeeder() {
            return () -> {
                Connector connector = getInstance().connector;
                try {
                    connector.addObserver(this);
                    this.publishSubject.subscribe();

                    synchronized (this) {
                        while (!stopped) {
                            for (E e = events.poll(); e != null; e = events.poll()) {
                                publishSubject.onNext(e);
                            }

                            try {
                                this.wait(200);
                            } catch (InterruptedException e) {
                                System.out.println("Interrupted: " + stopped + ", " + events.size());
                            }
                        }

                        publishSubject.onComplete();
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
                ResponseHeader header = mapper.readValue(message, ResponseHeader.class);
                if (header.getId() == feedId && "feed".equals(header.getResponse())) {
                    events.offer(mapper.readValue(message, eventsType));
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }

        public void stop() {
            stopped = true;
        }

        public Observable<E> getObservable() {
            return publishSubject;
        }
    }
}
