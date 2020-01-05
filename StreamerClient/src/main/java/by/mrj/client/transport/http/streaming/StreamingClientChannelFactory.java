package by.mrj.client.transport.http.streaming;

import by.mrj.client.service.MessageConsumer;
import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.client.transport.http.AuthenticationHttpHandler;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.serialization.DataSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StreamingClientChannelFactory implements ClientChannelFactory {

    private final DataSerializer dataSerializer;
    private final MessageConsumer messageConsumer;
    @Setter
    private ChannelFutureListener handshakeListener = future -> {
        // NOOP
    };

    @Override
    public by.mrj.client.transport.ServerChannel createChannel(EventLoopGroup group, ConnectionInfo connectionInfo) {

        final String host = connectionInfo.getHost();
        final Integer port = connectionInfo.getPort();
        final SslContext sslCtx = connectionInfo.getSslCtx();

        var authHandler = new AuthenticationHttpHandler();

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
                        p.addLast("authorizationHandler", authHandler);
                        p.addLast(new StreamingClientDefaultResponseContentHandler());
                        p.addLast("httpContent", new StreamingClientHttpContentHandler(messageConsumer, connectionInfo));
                        p.addLast("fullResponse", new StreamingClientFullResponseHandler(messageConsumer, connectionInfo));
                        p.addLast(new SimpleChannelInboundHandler<Object>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.error("#################################################");
                                log.error("### NONE PROCESSED MESSAGE [{}]", msg);
                                log.error("#################################################");
                            }
                        });
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);

        log.info("Connecting...");

        try {
            ChannelFuture channelFuture = b.connect(host, port);
            log.info("ST connection established");

            channelFuture.addListener(handshakeListener);

            Channel channel = channelFuture.sync().channel();

            StreamingServerChannel streamingServerChannel = new StreamingServerChannel(channel, dataSerializer);

            authorize(connectionInfo, authHandler, streamingServerChannel);

            return streamingServerChannel;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConnectionType connectionType() {
        return ConnectionType.HTTP_STREAMING;
    }
}
