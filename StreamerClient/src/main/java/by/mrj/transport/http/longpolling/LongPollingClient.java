package by.mrj.transport.http.longpolling;

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
import io.netty.handler.codec.http.*;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@State(Scope.Benchmark)
@RequiredArgsConstructor
public class LongPollingClient {

    // todo: configurable
    static final String URL = System.getProperty("url", "http://127.0.0.1:8083/");

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
            URI uri = getUri();

            String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
            final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();

            final int port = getPort(uri, scheme);

            checkProtocol(scheme);

            final SslContext sslCtx = getSslContext(scheme);

            EventLoopGroup group = new NioEventLoopGroup();
            try {

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
                                p.addLast(new HttpObjectAggregator(8192));
//                                    WebSocketClientCompressionHandler.INSTANCE,
                                p.addLast(new LongPoolingHttpClientTextHandler());
                            }
                        })
                        .option(ChannelOption.SO_KEEPALIVE, true)
                ;

                try {
                    this.channel = b.connect(uri.getHost(), port).sync().channel();

                    this.channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                group.shutdownGracefully();
            }
        });
    }

    private URI getUri() {
        URI uri;
        try {
            uri = new URI(URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    private void checkProtocol(String scheme) {
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            log.error("Only WS(S) is supported.");
            throw new RuntimeException("Only WS(S) is supported.");
        }
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

        // Prepare the HTTP request.
        String rawPath = getUri().getRawPath();
        HttpRequest request =
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, rawPath, message, false);
        request.headers().set(HttpHeaderNames.HOST, "127.0.0.1");
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, message.readableBytes());


        // Set some example cookies.
        request.headers().set(HttpHeaderNames.COOKIE, "MyCookie=12345");

        channel.writeAndFlush(request);
    }

    @SneakyThrows
    private SslContext getSslContext(String scheme) {
        final boolean ssl = "https".equalsIgnoreCase(scheme);
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
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }
        return port;
    }
}
