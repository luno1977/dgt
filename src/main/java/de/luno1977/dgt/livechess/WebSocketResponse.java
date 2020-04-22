package de.luno1977.dgt.livechess;

import de.luno1977.dgt.livechess.model.EBoardResponse;
import de.luno1977.dgt.livechess.model.SourceResponse;

import java.util.List;

public interface WebSocketResponse<P> extends WebSocketCommunication<P> {
    String getResponse();
    long getTime();

    class EBoardsResponse extends Base<List<EBoardResponse>> {}
    class SourcesResponse extends Base<List<SourceResponse>> {}
    class Ack extends Base<Object> {}

    abstract class Base<P> implements WebSocketResponse<P> {
        private String response;
        private long id;
        private P param;
        private long time;

        @Override
        public String getResponse() {
            return null;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        @Override
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        @Override
        public P getParam() {
            return param;
        }

        public void setParam(P param) {
            this.param = param;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()+"{" +
                    "response='" + response + '\'' +
                    ", id=" + id +
                    ", param=" + param +
                    ", time=" + time +
                    '}';
        }
    }

}
