package de.luno1977.dgt.livechess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public class SourceResponse {

    private final String type;
    private final String device;
    @Nullable private final String name;
    private final boolean present;

    private final boolean active;
    @Nullable private final String status;

    @JsonCreator
    public SourceResponse(
            @JsonProperty("type") String type,
            @JsonProperty("device") String device,
            @JsonProperty("name") @Nullable String name,
            @JsonProperty("present") boolean present,
            @JsonProperty("active") boolean active,
            @JsonProperty("status") @Nullable String status) {
        this.type = type;
        this.device = device;
        this.name = name;
        this.present = present;
        this.active = active;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public String getDevice() {
        return device;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public boolean isPresent() {
        return present;
    }

    public boolean isActive() {
        return active;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "SourceResponse{" +
                "type='" + type + '\'' +
                ", device='" + device + '\'' +
                ", name='" + name + '\'' +
                ", present=" + present +
                ", active=" + active +
                ", status='" + status + '\'' +
                '}';
    }
}
