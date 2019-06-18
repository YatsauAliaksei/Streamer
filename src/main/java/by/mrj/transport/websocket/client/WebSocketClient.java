package by.mrj.transport.websocket.client;

import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import by.mrj.serialization.json.JsonJackson;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@State(Scope.Benchmark)
@RequiredArgsConstructor
public class WebSocketClient {

    // todo: configurable
    static final String URL = System.getProperty("url", "ws://127.0.0.1:8080/websocket");

    public Channel channel;
    private final JsonJackson serializer = new JsonJackson();
    private ChannelFutureListener handshakeListener = future -> {
        // NOOP
    };

    public void registerHandshakeListener(ChannelFutureListener listener) {
        this.handshakeListener = listener;
    }

    public void createChannel() {
        CompletableFuture.runAsync(() -> {
            URI uri;
            try {
                uri = new URI(URL);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
            final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();

            final int port = getPort(uri, scheme);

            if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                log.error("Only WS(S) is supported.");
                throw new RuntimeException("Only WS(S) is supported.");
            }

            final SslContext sslCtx;
            try {
                sslCtx = getSslContext(scheme);
            } catch (SSLException e) {
                throw new RuntimeException(e);
            }

            EventLoopGroup group = new NioEventLoopGroup();
            try {
                // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
                // If you change it to V00, ping is not supported and remember to change
                // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
                final WebSocketClientInHandler handler = getWebSocketClientHandler(uri);

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
                                p.addLast(
                                        new HttpClientCodec(),
                                        new HttpObjectAggregator(8192),
//                                    WebSocketClientCompressionHandler.INSTANCE,
                                        handler
                                );
                            }
                        })
                        .option(ChannelOption.SO_KEEPALIVE, true)
                ;

                try {
                    this.channel = b.connect(uri.getHost(), port).sync().channel();
                    handler.handshakeFuture().addListener(handshakeListener).sync();// waiting for handshake

                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                group.shutdownGracefully();
            }
        });
    }

    @SneakyThrows
    public void closeFutureSync() {
        channel.closeFuture().sync();
    }

    public void send(Message<?> msg, MessageHeader messageHeader) {
        log.debug("Sending msg [{}]", msg);

        ByteBuf header = Unpooled.buffer();
        header.writeCharSequence(serializer.serialize(messageHeader), CharsetUtil.UTF_8);

        ByteBuf body = Unpooled.buffer();
        body.writeCharSequence(JsonJackson.toJson(msg), CharsetUtil.UTF_8);

        ByteBuf message = Unpooled.wrappedBuffer(3,
                Unpooled.buffer(4, 4).writeInt(header.readableBytes()), // header size
                header,
                body
        );

        WebSocketFrame frame = new BinaryWebSocketFrame(message);
        channel.writeAndFlush(frame);
    }

    private WebSocketClientInHandler getWebSocketClientHandler(URI uri) {
        return new WebSocketClientInHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                        uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
                        //todo: set authorization header
//                                .add(HttpHeaderNames.AUTHORIZATION, "bot-" + random.nextInt(0, 10_000) + ":PasSw0rd")
                )
        );
    }

    private SslContext getSslContext(String scheme) throws SSLException {
        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }
        return sslCtx;
    }

    private int getPort(URI uri, String scheme) {
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }
        return port;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /*    private void consoleSender(Channel ch) throws IOException, InterruptedException {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String msg = console.readLine();
            if (msg == null) {
                break;
            } else if ("bye".equals(msg.toLowerCase())) {
                ch.writeAndFlush(new CloseWebSocketFrame());
                ch.closeFuture().sync();
                break;
            } else if ("ping".equals(msg.toLowerCase())) {
                WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
                ch.writeAndFlush(frame);
            } else {
//                send(ch, msg);
            }
        }
    }*/
}
