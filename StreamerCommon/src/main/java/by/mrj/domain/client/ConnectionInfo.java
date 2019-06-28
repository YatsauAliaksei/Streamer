package by.mrj.domain.client;

import lombok.ToString;

@ToString
public class ConnectionInfo {

    private String scheme;
    private String host;

    public static ConnectionInfo from(ClientConnection clientConnection) {
        return new ConnectionInfo();
    }

    public static ConnectionInfo from(String scheme, String host) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.scheme = scheme;
        connectionInfo.host = host;
        return connectionInfo;
    }
}
