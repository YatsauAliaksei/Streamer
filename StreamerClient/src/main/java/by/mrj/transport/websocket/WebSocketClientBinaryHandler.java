package by.mrj.transport.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketClientBinaryHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
        log.debug("WebSocket Client received message: " + frame.content());
    }
}
