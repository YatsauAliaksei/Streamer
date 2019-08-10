package by.mrj.client.transport.http.streaming;

import by.mrj.client.transport.ServerChannel;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StreamingServerChannel implements ServerChannel {

    @Getter
    private final Channel channel;
    private final DataSerializer dataSerializer;

    @Override
    public void send(Message<?> msg, MessageHeader messageHeader) {
        log.debug("Sending msg [{}]", msg);

        ByteBuf message = ByteBufUtils.create(dataSerializer, messageHeader, msg);

        // Prepare the HTTP request.
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "/" + ConnectionType.HTTP_STREAMING.getUri(), message, false);
//        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        request.headers().set(HttpHeaderNames.AUTHORIZATION, "Basic my:password");
        request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, message.readableBytes());

        // Set some example cookies.
        request.headers().set(HttpHeaderNames.COOKIE, "MyCookie=12345");

        channel.writeAndFlush(request);
    }

    @Override
    @SneakyThrows
    public void closeFutureSync() {
        channel.closeFuture().sync();
    }
}
