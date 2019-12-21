package by.mrj.client.transport.websocket;

import by.mrj.client.service.MessageConsumer;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class WebSocketClientTextHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final MessageConsumer messageConsumer;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        BaseObject[] msg = JsonJackson.fromJson(frame.text(), BaseObject[].class);

        if (log.isDebugEnabled()) {
            log.debug("WebSocket Client received message: {}", Arrays.toString(msg));
        }

        messageConsumer.consume(msg);
    }

    @Override
    public String toString() {
        return "WS text handler";
    }
}
