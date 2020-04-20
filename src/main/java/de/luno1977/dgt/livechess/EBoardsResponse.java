package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EBoardsResponse implements WebSocketResponse {

    private String response;
    private long id;
    private Object param;

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
    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "EBoardsResponse{" +
                "response='" + response + '\'' +
                ", id=" + id +
                ", param=" + param +
                '}';
    }
}
