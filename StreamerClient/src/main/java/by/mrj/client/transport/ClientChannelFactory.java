package by.mrj.client.transport;

import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import io.netty.channel.EventLoopGroup;

public interface ClientChannelFactory {

    ServerChannel createChannel(EventLoopGroup group, ConnectionInfo connectionInfo);

    ConnectionType connectionType();
}
