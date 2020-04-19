package de.luno1977.dgt.livechess;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class LiveChessConnectorTest {

    private class TestMessageHandler implements LiveChessConnector.MessageHandler {
        private List<String> messages = new ArrayList<String>();
        public List<String> getMessages() { return messages; }
        public void handleMessage(String message) { messages.add(message); }
    }

    @Test
    public void testConnection() throws Exception {

        final LiveChessConnector liveChessConnection =
                new LiveChessConnector(new URI("ws://localhost:1982/api/v1.0"));

        TestMessageHandler handler = new TestMessageHandler() {
            public void handleMessage(String message) {
                super.handleMessage(message);
                Assert.assertTrue(message.contains(
                        "\"response\":\"call\",\"id\":36,\"param\":[{\"serialnr\""));
            }
        };

        liveChessConnection.addMessageHandler(handler);
        liveChessConnection.sendMessage("{\n" +
                "    \"call\": \"eboards\",\n" +
                "    \"id\": 36,\n" +
                "    \"param\": null\n" +
                "}");

        Thread.sleep(3000);
        System.out.println(handler.getMessages());
        Assert.assertEquals(1, handler.getMessages().size());
    }
}
