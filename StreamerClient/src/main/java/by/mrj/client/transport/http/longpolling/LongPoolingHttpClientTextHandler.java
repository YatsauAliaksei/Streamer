package by.mrj.client.transport.http.longpolling;

import by.mrj.client.service.MessageConsumer;
import by.mrj.client.transport.ServerChannelAware;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.EventListener;

@Slf4j
@RequiredArgsConstructor
public class LongPoolingHttpClientTextHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final MessageConsumer messageConsumer;
    private final ConnectionInfo connectionInfo;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("LongPolling Client disconnected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        log.debug("LongPolling Http Client received message: [{}]", response);

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
        return "Long Polling text handler";
    }
}
