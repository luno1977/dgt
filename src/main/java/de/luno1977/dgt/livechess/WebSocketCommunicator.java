package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class WebSocketCommunicator {

    private static WebSocketCommunicator instance;

    public static WebSocketCommunicator getInstance() {
        if (instance == null) {
            instance = new WebSocketCommunicator();
        }

        return instance;
    }

    private final LiveChessConnector liveChessConnector;
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    private WebSocketCommunicator() {
        liveChessConnector = new LiveChessConnector(
                LiveChessConfig.getInstance().getLiveChessWebSocketEndpoint());
    }

    public EBoardsResponse getEBoards() {
        try {
            EBoardsCallHandler handler = new EBoardsCallHandler();
            executor.submit(handler).get(3, TimeUnit.SECONDS);
            return handler.getResult();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SourcesResponse getSources() {
        return null;
    }

    public void subscribe(WebSocketFeed feed) {

    }

    public void unsubscribe(WebSocketFeed feed) {

    }


    private abstract static class CallHandler<I extends WebSocketCall<?>, R extends WebSocketResponse>  implements Runnable, Observer {
        protected static final AtomicLong REQUEST_IDS = new AtomicLong(0l);
        protected final ObjectMapper mapper = new ObjectMapper();
        private final I input;
        private R result;
        private boolean stopped = false;

        protected CallHandler(I input) {
            this.input = input;
        }

        protected abstract R mapValue(String jsonResponse) throws JsonProcessingException;

        @Override
        public void run() {
            LiveChessConnector liveChessConnector = getInstance().liveChessConnector;
            try {
                liveChessConnector.addObserver(this);
                String message = mapper.writeValueAsString(input);
                liveChessConnector.sendMessage(message);

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
                liveChessConnector.deleteObserver(this);
            }
        }

        @Override
        public void update(Observable o, Object msg) {
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

    private class EBoardsCallHandler extends CallHandler<EBoardsCall, EBoardsResponse> {
        protected EBoardsCallHandler() {
            super(new EBoardsCall(REQUEST_IDS.getAndIncrement()));
        }

        @Override
        protected EBoardsResponse mapValue(String jsonResponse) throws JsonProcessingException {
            return mapper.readValue(jsonResponse, EBoardsResponse.class);
        }
    }
}
