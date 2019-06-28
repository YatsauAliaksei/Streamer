package by.mrj.transport.http.longpolling;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LongPoolingHttpClientTextHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("LongPolling Client disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        log.debug("LongPolling Http Client received message: [{}]", response.content().toString(CharsetUtil.UTF_8));
    }
}
