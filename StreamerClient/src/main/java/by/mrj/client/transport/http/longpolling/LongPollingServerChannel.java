package by.mrj.client.transport.http.longpolling;

import by.mrj.client.transport.ServerChannel;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LongPollingServerChannel implements ServerChannel {

    @Getter
    private final Channel channel;
    private final DataSerializer dataSerializer;

    @Override
    public ChannelFuture send(Message<?> msg, MessageHeader messageHeader) {
        log.debug("Sending msg [{}]", msg);

        ByteBuf message = ByteBufUtils.create(dataSerializer, messageHeader, msg);

        HttpRequest request = getHttpRequest(message);

        return channel.writeAndFlush(request);
    }

    @Override
    public ChannelFuture send(List<BaseObject> postData) {
        log.info("Posting data [{}]", postData);

        ByteBuf message = ByteBufUtils.createPost(postData);

        HttpRequest request = getHttpRequest(message);

        return channel.writeAndFlush(request);
    }

    private HttpRequest getHttpRequest(ByteBuf message) {
        // Prepare the HTTP request.
        // todo: so far same as StreamingServerChannel
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "/" + ConnectionType.HTTP_LP.getUri(), message, false);
//        request.headers().set(HttpHeaderNames.HOST, "127.0.0.1");
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        request.headers().set(HttpHeaderNames.AUTHORIZATION, "Basic LP-client:password");
        request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, message.readableBytes());


        // Set some example cookies.
        request.headers().set(HttpHeaderNames.COOKIE, "MyCookie=12345");
        return request;
    }

    @Override
    @SneakyThrows
    public void closeFutureSync() {
        channel.closeFuture().sync();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
