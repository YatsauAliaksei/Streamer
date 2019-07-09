package by.mrj.transport.http.streaming;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamingClientTextHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("LongPolling Client disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        log.debug("LongPolling Http Client received message: [{}]", response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause != null) {
            log.error("Error processing response", cause);
        }
        super.exceptionCaught(ctx, cause);
    }
}
