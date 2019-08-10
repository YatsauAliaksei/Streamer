package by.mrj.client.transport.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketClientPongHandler extends SimpleChannelInboundHandler<PongWebSocketFrame> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, PongWebSocketFrame frame) throws Exception {
        log.debug("WebSocket Client received pong");
    }
}
