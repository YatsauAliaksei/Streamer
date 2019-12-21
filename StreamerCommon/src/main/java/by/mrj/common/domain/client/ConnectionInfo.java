package by.mrj.common.domain.client;

import by.mrj.common.domain.ConnectionType;
import io.netty.handler.ssl.SslContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString(exclude = {"password", "sslCtx"})
public class ConnectionInfo {

    private ConnectionType connectionType;
    @EqualsAndHashCode.Exclude
    private SslContext sslCtx;
    private String host;
    private Integer port;
    private String login;
    private String password;

    public static ConnectionInfo from(ConnectionType scheme, SslContext sslCtx, String host, Integer port) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.connectionType = scheme;
        connectionInfo.host = host;
        connectionInfo.port = port;
        connectionInfo.sslCtx = sslCtx;
        return connectionInfo;
    }

    // fixme: remove login
    public static ConnectionInfo from(ConnectionType scheme, SslContext sslCtx, String host, Integer port, String login, String password) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.connectionType = scheme;
        connectionInfo.host = host;
        connectionInfo.port = port;
        connectionInfo.sslCtx = sslCtx;
        connectionInfo.login = login;
        connectionInfo.password = password;
        return connectionInfo;
    }
}
