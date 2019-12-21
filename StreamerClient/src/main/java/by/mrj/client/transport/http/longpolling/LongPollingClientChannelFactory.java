package by.mrj.client.transport.http.longpolling;

import by.mrj.client.service.MessageConsumer;
import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.client.transport.ServerChannel;
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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LongPollingClientChannelFactory implements ClientChannelFactory {

    private final DataSerializer dataSerializer;
    private final MessageConsumer messageConsumer;
    @Setter
    private ChannelFutureListener handshakeListener = future -> {
        // NOOP
    };

    @Override
    @SneakyThrows
    public ServerChannel createChannel(EventLoopGroup group, ConnectionInfo connectionInfo) {

        final String host = connectionInfo.getHost();
        final Integer port = connectionInfo.getPort();
        final SslContext sslCtx = connectionInfo.getSslCtx();
        var handler = new LongPoolingHttpClientTextHandler(messageConsumer);
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
//                        p.addLast(new HttpObjectAggregator(1 << 16));
                        p.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
//                                    WebSocketClientCompressionHandler.INSTANCE,
                        p.addLast(authHandler);
                        p.addLast(handler);
                        p.addLast(new SimpleChannelInboundHandler<Object>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.error("#################################################");
                                log.error("### NONE PROCESSED MESSAGE DETECTED [{}]", msg);
                                log.error("#################################################");
                            }
                        });
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);

        log.info("Connecting...");

        ChannelFuture channelFuture = b.connect(host, port);
        channelFuture.addListener(handshakeListener);

        Channel channel = channelFuture.sync().channel();
        log.info("LP channel connection established");

        LongPollingServerChannel longPollingServerChannel = new LongPollingServerChannel(channel, dataSerializer, handler);
        authorize(connectionInfo, authHandler, longPollingServerChannel);

        return longPollingServerChannel;
    }

    @Override
    public ConnectionType connectionType() {
        return ConnectionType.HTTP_LP;
    }
}
