package by.mrj.domain.client;

import lombok.ToString;

@ToString
public class ConnectionInfo {

    public static ConnectionInfo from(ClientConnection clientConnection) {
        return new ConnectionInfo();
    }
}
