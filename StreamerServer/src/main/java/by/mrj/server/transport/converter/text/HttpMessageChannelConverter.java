package by.mrj.server.transport.converter.text;

import by.mrj.common.transport.converter.MessageChannelConverter;
import by.mrj.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpMessageChannelConverter implements MessageChannelConverter<FullHttpResponse> {

    @Override
    public FullHttpResponse convert(String dataToSend) {

        log.trace("Sending data [{}]", dataToSend);

        ByteBuf msgBuf = ByteBufUtils.create(dataToSend);
        return this.convert(msgBuf);
    }

    @Override
    public FullHttpResponse convert(ByteBuf dataToSend) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, dataToSend);
        response.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, dataToSend.readableBytes());
        HttpUtil.setKeepAlive(response, true);

        return response;
    }

    @Override
    public String toString() {
        return "HttpMessageChannelConverter";
    }
}
