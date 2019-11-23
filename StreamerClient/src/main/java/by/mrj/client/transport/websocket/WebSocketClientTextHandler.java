package by.mrj.client.transport.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketClientTextHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        log.info("WebSocket Client received message: " + frame.text());
    }
}
