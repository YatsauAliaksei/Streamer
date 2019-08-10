package by.mrj.common.domain;

import lombok.Getter;

import java.util.Arrays;

public enum ConnectionType {

    WS("type3"),
    HTTP("type4"),
    HTTP_LP("type2"),
    HTTP_STREAMING("type1"),
    UNKNOWN("unknown");

    @Getter
    private final String uri;

    ConnectionType(String uri) {
        this.uri = uri;
    }

    public static ConnectionType byUri(String uri) {

        return Arrays.stream(ConnectionType.values())
                .filter(type -> type.uri.equalsIgnoreCase(uri))
                .findFirst()
                .orElse(ConnectionType.UNKNOWN);
    }
}
