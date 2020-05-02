package de.luno1977.dgt.livechess;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ConnectorTest {

    private static class TestMessageHandler implements Observer {
        private final List<String> messages = new ArrayList<>();
        public List<String> getMessages() { return messages; }

        @Override
        public void update(Observable o, Object msg) {
            messages.add(msg.toString());
            synchronized (messages) {
                messages.notifyAll();
            }
        }
    }

    @Test @Ignore
    public void testConnection() throws Exception {

        final LiveChess.Connector liveChessConnection =
                new LiveChess.Connector(LiveChessConfig.getInstance().getLiveChessWebSocketEndpoint());

        final TestMessageHandler handler = new TestMessageHandler() {
            public void update(Observable o, Object msg) {
                super.update(o, msg);
                Assert.assertTrue(msg.toString().contains(
                        "\"response\":\"call\",\"id\":36,\"param\":[{\"serialnr\""));
            }
        };

        liveChessConnection.addObserver(handler);
        liveChessConnection.sendMessage("{\n" +
                "    \"call\": \"eboards\",\n" +
                "    \"id\": 36,\n" +
                "    \"param\": null\n" +
                "}");

        synchronized (handler.messages) {
            int n = 0;
            while (handler.getMessages().size() < 1 && n < 5) {
                try {
                    handler.messages.wait(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    n++;
                }
            }
        }

        System.out.println(handler.getMessages());
        Assert.assertEquals(1, handler.getMessages().size());
        liveChessConnection.deleteObserver(handler);
    }
}
