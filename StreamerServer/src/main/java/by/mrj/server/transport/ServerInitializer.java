package by.mrj.server.transport;

import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.transport.converter.MessageChannelConverter;
import by.mrj.server.controller.CommandListener;
import by.mrj.server.security.jwt.JWTFilter;
import by.mrj.server.transport.http.HttpSecurityServerHandler;
import by.mrj.server.transport.http.FullHttpRequestHandler;
import by.mrj.server.transport.websocket.server.WebSocketFrameHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/type3";

    private final SslContext sslCtx;
    private final CommandListener commandListener;
    private final MessageChannelConverter<FullHttpResponse> httpMessageChannelConverter;
    private final MessageChannelConverter<WebSocketFrame> wsMessageChannelConverter;
    private final DataSerializer serializer;
    private final JWTFilter jwtFilter;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }

        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpServerKeepAliveHandler());
        pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
//        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new ChunkedWriteHandler()); // http streaming support
        pipeline.addLast(new HttpSecurityServerHandler(jwtFilter));
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true, Integer.MAX_VALUE));
//        pipeline.addLast(new ValidationHandler());
        // todo: remove
        pipeline.addLast(new SimpleChannelInboundHandler<Object>() { /* Logging temporary */


            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("Message 0 DEBUG [{}]", msg);
                ctx.fireChannelRead(msg);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("Message DEBUG [{}]", msg);
                ctx.fireChannelRead(msg);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                if (cause != null) {
                    log.error("Inbound message processing failed.", cause);
                }
                super.exceptionCaught(ctx, cause);
            }
        });
        //todo: think about removing this if WS established
        pipeline.addLast(new FullHttpRequestHandler(commandListener, httpMessageChannelConverter, serializer));
        pipeline.addLast(new WebSocketFrameHandler(commandListener, wsMessageChannelConverter));
        pipeline.addLast(new SimpleChannelInboundHandler<Object>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.error("#################################################");
                log.error("### NONE PROCESSED MESSAGE DETECTED [{}]", msg);
                log.error("#################################################");
            }
        });
    }
}
