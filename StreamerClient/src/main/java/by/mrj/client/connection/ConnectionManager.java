package by.mrj.client.connection;

import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ConnectionManager {

    private final Map<ConnectionType, ClientChannelFactory> typeToFactory;
    private final AutoConnectionInfoFactory connectionInfoFactory;
    private Map<ConnectionInfo, ServerChannelHolder> connectionToChannel = new ConcurrentHashMap<>();

    /**
     * Creates connection using {@param connectionInfo}.
     * Connection type is automatically chosen.
     * @return - {@link ServerChannelHolder}
     */
    public ServerChannelHolder autoConnect() {
        ConnectionInfo ci = connectionInfoFactory.get();
        return connect(ci);
    }

    public ServerChannelHolder connect(ConnectionInfo connectionInfo) {

        ClientChannelFactory clientChannelFactory = typeToFactory.get(connectionInfo.getConnectionType());
        if (!validate(clientChannelFactory)) {
            // fixme
        }

        return createServerChannelHolder(connectionInfo, clientChannelFactory);
    }

    private ServerChannelHolder createServerChannelHolder(ConnectionInfo connectionInfo, ClientChannelFactory clientChannelFactory) {
        ServerChannelHolder serverChannelHolder = new ServerChannelHolder();
        serverChannelHolder.createChannel(clientChannelFactory, connectionInfo);

        ServerChannelHolder prevServerChannelHolder;
        if ((prevServerChannelHolder = connectionToChannel.put(connectionInfo, serverChannelHolder)) != null) {
            prevServerChannelHolder.getChannel().getChannel().close();
        }

        return serverChannelHolder;
    }

    private boolean validate(ClientChannelFactory clientChannelFactory) {
        return true;
    }

    public void closeConnection(ConnectionInfo connectionInfo) {
        // fixme
    }

    public ServerChannelHolder findChannel(ConnectionInfo connectionInfo) {
        return connectionToChannel.getOrDefault(connectionInfo, null);
    }

}
