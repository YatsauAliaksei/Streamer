package by.mrj.domain;


import by.mrj.domain.client.ConnectionInfo;
import by.mrj.transport.converter.MessageChannelConverter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


@Getter
@ToString
@RequiredArgsConstructor
public class StreamingChannel {

    private final Channel channel;
    private final ConnectionInfo connectionInfo;
    private final MessageChannelConverter<String, ?> messageChannelConverter;

    public void writeAndFlush(String toSend) {
        channel.writeAndFlush(messageChannelConverter.convert(toSend));
    }

    public void write(String toSend) {
        channel.write(messageChannelConverter.convert(toSend));
    }

    public void flush() {
        channel.flush();
    }

    public static StreamingChannel from(ChannelHandlerContext ctx, String scheme, MessageChannelConverter<String, ?> messageChannelConverter) {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        String host = null;
        if (socketAddress instanceof InetSocketAddress) {
            host = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
        }

        return new StreamingChannel(ctx.channel(), ConnectionInfo.from(scheme, host), messageChannelConverter);
    }
}
