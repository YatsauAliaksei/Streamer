package by.mrj.client.connection;

import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConnectionInfoFactory {

    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

    public ConnectionInfo auto() {
        // todo: should be picked up considering prev attempts
        return ConnectionInfo.from(ConnectionType.WS, null, host, port, "WS-login", "pwd");
    }

    public ConnectionInfo webSocketConnectionInfo() {
        return ConnectionInfo.from(ConnectionType.WS, null, host, port, "WS-login", "pwd");
    }

    public ConnectionInfo longPollingConnectionInfo() {
        return ConnectionInfo.from(ConnectionType.HTTP_LP, null, host, port, "WS-login", "pwd");
    }

    public ConnectionInfo StreamingConnectionInfo() {
        return ConnectionInfo.from(ConnectionType.HTTP_STREAMING, null, host, port, "WS-login", "pwd");
    }
}
