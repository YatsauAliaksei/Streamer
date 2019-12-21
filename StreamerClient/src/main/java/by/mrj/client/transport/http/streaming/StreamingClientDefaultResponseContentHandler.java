package by.mrj.client.transport.http.streaming;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamingClientDefaultResponseContentHandler extends SimpleChannelInboundHandler<DefaultHttpResponse> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Streaming Client disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DefaultHttpResponse response) throws Exception {
        log.warn("Streaming Http Client received DEFAULT message: [{}]", response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause != null) {
            log.error("Error processing response", cause);
        }
        super.exceptionCaught(ctx, cause);
    }
}
