package by.mrj.domain;


import by.mrj.domain.client.ConnectionInfo;
import by.mrj.transport.converter.MessageChannelConverter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


@Slf4j
@Getter
@ToString
@RequiredArgsConstructor
public class StreamingChannel {

    private final Channel channel;
    private final ConnectionInfo connectionInfo;
    private final MessageChannelConverter<String, ?> textMessageChannelConverter;
    private final MessageChannelConverter<ByteBuf, ?> byteMessageChannelConverter;

    public void writeAndFlush(String toSend) {
        channel.writeAndFlush(textMessageChannelConverter.convert(toSend));
    }

    public void writeAndFlush(ByteBuf toSend) {
        channel.writeAndFlush(byteMessageChannelConverter.convert(toSend));
    }

    public void write(String toSend) {
        channel.write(textMessageChannelConverter.convert(toSend));
    }

    public void write(ByteBuf toSend) {
        channel.write(byteMessageChannelConverter.convert(toSend));
    }

    public void flush() {
        channel.flush();
    }

    public static StreamingChannel from(ChannelHandlerContext ctx, String scheme,
                                        MessageChannelConverter<String, ?> textMessageChannelConverter,
                                        MessageChannelConverter<ByteBuf, ?> byteMessageChannelConverter) {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        String host = null;
        if (socketAddress instanceof InetSocketAddress) {
            host = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
        }

        return new StreamingChannel(ctx.channel(), ConnectionInfo.from(scheme, host),
                textMessageChannelConverter, byteMessageChannelConverter);
    }
}
