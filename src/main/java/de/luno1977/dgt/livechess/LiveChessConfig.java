package de.luno1977.dgt.livechess;

import java.net.URI;
import java.net.URISyntaxException;

public class LiveChessConfig {

    private static LiveChessConfig instance;

    private LiveChessConfig() { /* singleton */ }

    public static LiveChessConfig getInstance() {
        if (instance == null) {
            instance = new LiveChessConfig();
        }

        return instance;
    }

    private URI liveChessWebSocketEndpoint;

    public URI getLiveChessWebSocketEndpoint() {
        if (liveChessWebSocketEndpoint == null) {
            try { liveChessWebSocketEndpoint = new URI("ws://localhost:1982/api/v1.0"); }
            catch (URISyntaxException e) { throw new RuntimeException(e); }
        }
        return liveChessWebSocketEndpoint;
    }

    public void setLiveChessWebSocketEndpoint(URI liveChessWebSocketEndpoint) {
        this.liveChessWebSocketEndpoint = liveChessWebSocketEndpoint;
    }
}
