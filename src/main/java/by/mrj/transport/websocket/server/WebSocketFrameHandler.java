package by.mrj.transport.websocket.server;

import by.mrj.controller.CommandListener;
import by.mrj.domain.StreamingChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final CommandListener commandListener;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.debug("Channel activated"); // invokes twice
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        log.debug("Msg received [{}]", frame);

        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            log.debug("Text frame read: [{}]", ((TextWebSocketFrame) frame).text());
            TextWebSocketFrame socketFrame = (TextWebSocketFrame) frame;
            String msg = socketFrame.text();
            commandListener.processRequest(msg, null, getStreamChannel(ctx));

//            ctx.channel().writeAndFlush(new TextWebSocketFrame(request));
        } else if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame socketFrame = (BinaryWebSocketFrame) frame;

            String header;
            String body;
            ByteBuf message = null;
            try {
                message = socketFrame.content();
                log.debug("Bytes to read: [{}]", message.readableBytes());

                int headerSize = message.readInt();
                header = message.readCharSequence(headerSize, CharsetUtil.UTF_8).toString();
                body = message.readCharSequence(message.readableBytes(), CharsetUtil.UTF_8).toString();
            } finally {
                if (message != null) {
                    message.release(message.refCnt());
                }
            }

            commandListener.processRequest(header, body, getStreamChannel(ctx));
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    private StreamingChannel getStreamChannel(ChannelHandlerContext ctx) {
        return new StreamingChannel(ctx.channel());
    }
}
