package de.luno1977.dgt.livechess;

import de.luno1977.dgt.livechess.WebSocketCall.EBoards;
import de.luno1977.dgt.livechess.WebSocketCall.Sources;
import de.luno1977.dgt.livechess.WebSocketResponse.EBoardsResponse;
import de.luno1977.dgt.livechess.WebSocketResponse.SourcesResponse;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
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

    public EBoardsResponse getEBoards() {
        try {
            EBoards.Handler handler = new EBoards.Handler(connector);
            executor.submit(handler).get(3, TimeUnit.SECONDS);
            return handler.getResult();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SourcesResponse getSources() {
        try {
            Sources.Handler handler = new Sources.Handler(connector);
            executor.submit(handler).get(3, TimeUnit.SECONDS);
            return handler.getResult();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public EBoardEventFeed subscribe() {
        EBoardEventFeed feed = new EBoardEventFeed();
        feed.subscribe();

        return feed;
    }

    public boolean unsubscribe(EBoardEventFeed feed) {
        feed.unsubscribe();
        return feed.isSubscribed();
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

}
