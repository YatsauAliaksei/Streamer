package by.mrj.common.domain.client.channel;

import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.transport.converter.MessageChannelConverter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
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
public class WebSocketClientChannel implements ClientChannel {

    private final Channel channel;
    private final ConnectionInfo connectionInfo;
    private final MessageChannelConverter<WebSocketFrame> textMessageChannelConverter;

    public void writeAndFlush(String toSend) {
        this.write(toSend);
        this.flush();
    }

    public void writeAndFlush(ByteBuf toSend) {
        this.write(toSend);
        this.flush();
    }

    public void write(String toSend) {
        channel.write(textMessageChannelConverter.convert(toSend));
    }

    public void write(ByteBuf toSend) {
        channel.write(textMessageChannelConverter.convert(toSend));
    }

    public void flush() {
        channel.flush();
    }

    public static WebSocketClientChannel from(ChannelHandlerContext ctx,
                                              MessageChannelConverter<WebSocketFrame> textMessageChannelConverter) {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        String host = null;
        Integer port = null;
        if (socketAddress instanceof InetSocketAddress) {
            host = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
            port = ((InetSocketAddress) socketAddress).getPort();
        }

        return new WebSocketClientChannel(ctx.channel(), ConnectionInfo.from(ConnectionType.WS, null, host, port),
                textMessageChannelConverter);
    }
}
