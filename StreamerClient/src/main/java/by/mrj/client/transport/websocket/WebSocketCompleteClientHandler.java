package by.mrj.client.transport.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketCompleteClientHandler extends SimpleChannelInboundHandler<DefaultHttpResponse> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    @Getter
    private String authHeader;

    public WebSocketCompleteClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DefaultHttpResponse msg) throws Exception {
        Channel ch = ctx.channel();
        log.debug("WS handshake not completed yet [{}]", msg);
        if (!handshaker.isHandshakeComplete()) {
            try {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, msg.status());
                response.headers().add(msg.headers());

                // todo: while we don't have JWT time limit this works
                authHeader = msg.headers().get(HttpHeaderNames.AUTHORIZATION);

                handshaker.finishHandshake(ch, response);
                log.debug("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                log.info("WebSocket Client failed to createChannel");
                handshakeFuture.setFailure(e);
            }
        }
        ctx.pipeline().remove(this);
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("WebSocket Client disconnected!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
        throw new RuntimeException(cause);
    }
}
