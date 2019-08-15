package by.mrj.client.transport;

import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.reactivex.Single;

public interface ClientChannelFactory {

    ServerChannel createChannel(EventLoopGroup group, ConnectionInfo connectionInfo);

    ConnectionType connectionType();

    void setHandshakeListener(ChannelFutureListener channelFutureListener);
}
