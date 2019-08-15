package by.mrj.client.connection;

import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.client.transport.ServerChannel;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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
    public Single<ServerChannelHolder> autoConnect() {
        ConnectionInfo ci = connectionInfoFactory.get();
        log.info("Trying to connect using [{}]", ci);
        return connect(ci);
    }

    /**
     * May return not yet initialized channel. Use {@link this#findChannel} instead if guarantee needed.
     * @param connectionInfo
     * @return
     */
    public Single<ServerChannelHolder> connect(ConnectionInfo connectionInfo) {

        ClientChannelFactory clientChannelFactory = typeToFactory.get(connectionInfo.getConnectionType());
        if (!validate(clientChannelFactory)) {
            // fixme
        }

        return createServerChannelHolder(connectionInfo, clientChannelFactory);
    }

    private Single<ServerChannelHolder> createServerChannelHolder(ConnectionInfo connectionInfo, ClientChannelFactory clientChannelFactory) {
        return Single.create(emitter -> {
            ServerChannelHolder serverChannelHolder = new ServerChannelHolder();

            Single<? extends ServerChannel> channelSingle = serverChannelHolder.createChannel(clientChannelFactory, connectionInfo);

            channelSingle.subscribe(ch -> {

                        ServerChannelHolder prevServerChannelHolder;

                        log.info("Saving connection channel [{}]", ch);

                        if ((prevServerChannelHolder = connectionToChannel.put(connectionInfo, serverChannelHolder)) != null) {
                            prevServerChannelHolder.closeFutureSync();
                        }
                        serverChannelHolder.rawChannel().closeFuture()
                                .addListener(closeFuture -> connectionToChannel.remove(connectionInfo));

                        emitter.onSuccess(serverChannelHolder);
                    },
                    e -> {
                        throw new RuntimeException(e);
                    });
        });
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
