package by.mrj.client.transport.http.streaming;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamingClientTextContentHandler extends SimpleChannelInboundHandler<DefaultHttpContent> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Streaming Client disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DefaultHttpContent content) throws Exception {
//        log.debug("LongPolling Http Client received message: [{}]", content.content());
        log.debug("Streaming Http Client received message: [{}]", content.content().toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause != null) {
            log.error("Error processing response", cause);
        }
        super.exceptionCaught(ctx, cause);
    }
}
