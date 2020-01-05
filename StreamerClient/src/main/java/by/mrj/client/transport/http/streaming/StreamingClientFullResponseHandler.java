package by.mrj.client.transport.http.streaming;

import by.mrj.client.service.MessageConsumer;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class StreamingClientFullResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final MessageConsumer messageConsumer;
    private final ConnectionInfo connectionInfo;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Streaming Client disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {

        log.info("Streaming Http Client received FULL message: [{}]", response);

        String content = response.content().toString(StandardCharsets.UTF_8);

        messageConsumer.consume(JsonJackson.fromJson(content, BaseObject[].class), connectionInfo);
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
        return "Streaming Text handler";
    }
}
