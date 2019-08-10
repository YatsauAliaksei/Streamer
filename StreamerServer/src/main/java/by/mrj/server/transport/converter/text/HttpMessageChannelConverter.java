package by.mrj.server.transport.converter.text;

import by.mrj.common.domain.Command;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.transport.converter.MessageChannelConverter;
import by.mrj.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpMessageChannelConverter implements MessageChannelConverter<FullHttpResponse> {

    private final DataSerializer dataSerializer;

    @Override
    public FullHttpResponse convert(String dataToSend) {

        log.debug("Sending data [{}]", dataToSend);

        ByteBuf msgBuf = ByteBufUtils.create(dataSerializer,
                MessageHeader.builder()
                        .command(Command.POST)
                        .build(),
                Message.<String>builder()
                        .payload(dataToSend)
                        .build());
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
}
