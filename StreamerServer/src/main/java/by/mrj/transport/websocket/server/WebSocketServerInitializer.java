package by.mrj.transport.websocket.server;

import by.mrj.controller.CommandListener;
import by.mrj.transport.HttpServerHandler;
import by.mrj.transport.converter.HttpMessageChannelConverter;
import by.mrj.transport.converter.MessageChannelConverter;
import by.mrj.transport.converter.WebSocketTextMessageChannelConverter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket";

    private final SslContext sslCtx;
    private final CommandListener commandListener;
    private final MessageChannelConverter<String, ?> httpMessageChannelConverter;
    private final MessageChannelConverter<String, ?> wsMessageChannelConverter;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpServerKeepAliveHandler());
        pipeline.addLast(new HttpObjectAggregator(65536));
//        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        pipeline.addLast(new HttpServerHandler(commandListener, httpMessageChannelConverter));
        pipeline.addLast(new WebSocketFrameHandler(commandListener, wsMessageChannelConverter));
    }
}
