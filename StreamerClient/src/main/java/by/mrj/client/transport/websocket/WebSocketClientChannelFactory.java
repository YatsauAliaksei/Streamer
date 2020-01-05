package by.mrj.client.transport.websocket;

import by.mrj.client.service.MessageConsumer;
import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.client.transport.ServerChannel;
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
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketClientChannelFactory implements ClientChannelFactory {

    private final DataSerializer dataSerializer;
    private final MessageConsumer messageConsumer;
    @Setter
    private ChannelFutureListener handshakeListener = future -> {
        // NOOP
    };
    @Getter
    private ChannelFuture handshakeFuture;

    @SneakyThrows
    @Override
    public ServerChannel createChannel(EventLoopGroup group, ConnectionInfo connectionInfo) {

        final String host = connectionInfo.getHost();
        final Integer port = connectionInfo.getPort();
        final SslContext sslCtx = connectionInfo.getSslCtx();

        // todo: https
        WebSocketClientHandshaker wsHandshaker =
                getWebSocketClientHandshaker(new URI("http://" + host + ":" + port + "/" + ConnectionType.WS.getUri())
                        , connectionInfo.getLogin(), connectionInfo.getPassword());

        WebSocketCompleteClientHandler webSocketCompleteClientHandler = new WebSocketCompleteClientHandler(wsHandshaker);

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
                        p.addLast(new LoggingHandler(LogLevel.DEBUG));
                        p.addLast(new HttpClientCodec());
//                                p.addLast(new HttpObjectAggregator(8192));
//                                    WebSocketClientCompressionHandler.INSTANCE,
                        p.addLast(webSocketCompleteClientHandler);
                        p.addLast(new WebSocketClientCloseHandler());
                        p.addLast(new WebSocketClientBinaryHandler());
                        p.addLast(new WebSocketClientPongHandler());
                        p.addLast(new WebSocketClientTextHandler(messageConsumer, connectionInfo));
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

        ChannelFuture channelFuture = b.connect(host, port);

        Channel channel = channelFuture.sync().channel();
        log.info("WS initial HTTP connection established");

        // waiting for handshake
        log.info("WS handshake started waiting...");
        handshakeFuture = webSocketCompleteClientHandler.handshakeFuture().addListener(handshakeListener);
        handshakeFuture.sync();

        log.info("WS handshake finished. Connected to {}:{}", host, port);
        log.info("WS connection established");
        return new WebSocketServerChannel(channel, dataSerializer);
    }

    @Override
    public ConnectionType connectionType() {
        return ConnectionType.WS;
    }

    // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
    // If you change it to V00, ping is not supported and remember to change
    // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
    private WebSocketClientHandshaker getWebSocketClientHandshaker(URI uri, String login, String password) {
        return WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
                        .add(HttpHeaderNames.AUTHORIZATION, "Basic " + login + ":" + password)
                , Integer.MAX_VALUE
        );
    }
}
