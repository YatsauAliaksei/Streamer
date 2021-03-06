package by.mrj.server.transport.websocket.server;

import by.mrj.common.domain.Command;
import by.mrj.common.domain.client.channel.WebSocketClientChannel;
import by.mrj.common.transport.converter.MessageChannelConverter;
import by.mrj.server.controller.CommandListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final CommandListener commandListener;
    private final MessageChannelConverter<WebSocketFrame> messageChannelConverter;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.debug("Channel activated"); // invokes twice
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled
        log.debug("Msg received [{}]", frame);

        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame socketFrame = (BinaryWebSocketFrame) frame;

            ByteBuf message = socketFrame.content();
            log.debug("Bytes to read: [{}]", message.readableBytes());

            int commandOrdinal = message.readInt();
            Command command = Command.byOrdinal(commandOrdinal);
//            String header = message.readCharSequence(command, CharsetUtil.UTF_8).toString();
//            String payload = message.readCharSequence(message.readableBytes(), CharsetUtil.UTF_8).toString();

            commandListener.processRequest(command, message, WebSocketClientChannel.from(ctx, messageChannelConverter));
        } else {
            throw new UnsupportedOperationException("Unsupported frame type: " + frame.getClass().getName());
        }
    }
}
