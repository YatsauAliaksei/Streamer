package by.mrj.client.transport.http.streaming;

import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.serialization.DataSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StreamingClientChannelFactory implements ClientChannelFactory {

    private final DataSerializer dataSerializer;
    @Setter
    private ChannelFutureListener handshakeListener = future -> {
        // NOOP
    };

    @Override
    public by.mrj.client.transport.ServerChannel createChannel(EventLoopGroup group, ConnectionInfo connectionInfo) {

        final String host = connectionInfo.getHost();
        final Integer port = connectionInfo.getPort();
        final SslContext sslCtx = connectionInfo.getSslCtx();

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        p.addLast(new HttpClientCodec());
//                                    WebSocketClientCompressionHandler.INSTANCE,
                        p.addLast(new StreamingClientDefaultResponseContentHandler());
                        p.addLast(new StreamingClientTextContentHandler());
                        p.addLast(new StreamingClientTextHandler());
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);

        try {
            ChannelFuture channelFuture = b.connect(host, port);
            log.info("LP connection established");

            channelFuture.addListener(handshakeListener);

            Channel channel = channelFuture.sync().channel();
            return new StreamingServerChannel(channel, dataSerializer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConnectionType connectionType() {
        return ConnectionType.HTTP_STREAMING;
    }
}
