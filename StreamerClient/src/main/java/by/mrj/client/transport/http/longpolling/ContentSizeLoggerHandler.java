package by.mrj.client.transport.http.longpolling;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ContentSizeLoggerHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("LongPolling Client disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        log.info("Content size handler: [{}]", response);

        ByteBuf bb = response.content();

        log.info("Readable bytes {}", bb.readableBytes());

        String content = bb.toString(CharsetUtil.UTF_8);

        log.info("Content: [{}]", content);

        BaseObject[] baseObjects = JsonJackson.fromJson(content, BaseObject[].class);

        if (baseObjects.length != 1 || baseObjects[0].getId() != null) {
            ctx.pipeline().remove(this);
        }

        response.retain();
        ctx.fireChannelRead(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause != null) {
            log.error("Error processing response", cause);
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public String toString() {
        return "Long Polling text handler";
    }
}
