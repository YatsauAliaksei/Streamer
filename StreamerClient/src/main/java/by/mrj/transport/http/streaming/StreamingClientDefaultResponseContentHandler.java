package by.mrj.transport.http.streaming;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamingClientDefaultResponseContentHandler extends SimpleChannelInboundHandler<DefaultHttpResponse> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("LongPolling Client disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DefaultHttpResponse response) throws Exception {
        log.debug("LongPolling Http Client received message: [{}]", response);
//        log.debug("LongPolling Http Client received message: [{}]", content.content().toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause != null) {
            log.error("Error processing response", cause);
        }
        super.exceptionCaught(ctx, cause);
    }
}
