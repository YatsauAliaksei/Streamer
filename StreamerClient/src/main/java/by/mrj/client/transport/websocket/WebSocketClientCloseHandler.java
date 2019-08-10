package by.mrj.client.transport.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketClientCloseHandler extends SimpleChannelInboundHandler<CloseWebSocketFrame> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, CloseWebSocketFrame frame) throws Exception {
        log.info("WebSocket Client received closing");
        ctx.channel().close();
    }
}
