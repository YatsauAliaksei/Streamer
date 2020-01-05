package by.mrj.client.connection;

import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ConnectionManager {

    private final Map<ConnectionType, ClientChannelFactory> typeToFactory;
    private final ConnectionInfoFactory connectionInfoFactory;
    private final ConnectionHolder connectionHolder;

    /**
     * Creates connection using {@param connectionInfo}.
     * Connection type is automatically chosen.
     * @return - {@link ServerChannelHolder}
     */
    public Single<ServerChannelHolder> autoConnect() {
        ConnectionInfo ci = connectionInfoFactory.auto();
        log.debug("Trying to connect using [{}]", ci);
        return connect(ci);
    }

    /**
     * May return not yet initialized channel. Use {@link ConnectionHolder#findChannel} instead if guarantee needed.
     * @param connectionInfo
     * @return
     */
    public Single<ServerChannelHolder> connect(ConnectionInfo connectionInfo) {

        ClientChannelFactory clientChannelFactory = typeToFactory.get(connectionInfo.getConnectionType());
        if (!validate(clientChannelFactory)) {
            // todo:
        }

        return createServerChannelHolder(connectionInfo, clientChannelFactory);
    }

    private Single<ServerChannelHolder> createServerChannelHolder(ConnectionInfo connectionInfo,
                                                                  ClientChannelFactory clientChannelFactory) {
        return Single.create(emitter -> {

            Single<ServerChannelHolder> channelSingle = ServerChannelHolder.create(clientChannelFactory, connectionInfo);

            channelSingle.subscribe(sch -> {

                        ServerChannelHolder prevServerChannelHolder;

                        log.debug("Saving connection channel [{}]", sch);

                        if ((prevServerChannelHolder = connectionHolder.put(connectionInfo, sch)) != null) {
                            prevServerChannelHolder.closeFutureSync();
                        }
                        sch.rawChannel().closeFuture()
                                .addListener(closeFuture -> connectionHolder.remove(connectionInfo));

                        emitter.onSuccess(sch);
                    },
                    e -> {
                        throw new RuntimeException(e);
                    });

            channelSingle.doOnError(t -> {
                log.error("Error creating channel [" + connectionInfo + "]", t);
            });
        });
    }

    private boolean validate(ClientChannelFactory clientChannelFactory) {
        return true;
    }

    public void closeConnection(ConnectionInfo connectionInfo) {
        // fixme
    }

}
