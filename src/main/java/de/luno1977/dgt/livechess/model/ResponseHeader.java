package de.luno1977.dgt.livechess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true)
public class ResponseHeader {

    private final long id;
    private final String response;

    @JsonCreator
    public ResponseHeader(
            @JsonProperty("id") long id,
            @JsonProperty("response") String response) {
        this.id = id;
        this.response = response;
    }

    public long getId() {
        return id;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "ResponseHeader{" +
                "id=" + id +
                ", response='" + response + '\'' +
                '}';
    }
}
