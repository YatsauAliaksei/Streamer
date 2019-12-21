package by.mrj.client.transport;

import by.mrj.client.transport.http.AuthenticationHttpHandler;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;

public interface ClientChannelFactory {

    ServerChannel createChannel(EventLoopGroup group, ConnectionInfo connectionInfo);

    ConnectionType connectionType();

    void setHandshakeListener(ChannelFutureListener channelFutureListener);

    default void authorize(ConnectionInfo connectionInfo, AuthenticationHttpHandler authHandler, ServerChannel serverChannel) {
        serverChannel.authorize(connectionInfo.getLogin(), connectionInfo.getPassword());

        String jwt = authHandler.getPromise().blockingGet();

        serverChannel.setAuthHeader(jwt);
    }
}
