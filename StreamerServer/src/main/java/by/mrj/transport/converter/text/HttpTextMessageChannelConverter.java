package by.mrj.transport.converter.text;

import by.mrj.domain.Command;
import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import by.mrj.serialization.DataSerializer;
import by.mrj.transport.converter.MessageChannelConverter;
import by.mrj.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class HttpTextMessageChannelConverter implements MessageChannelConverter<String, FullHttpResponse> {

    private final DataSerializer serializer;

    @Override
    public FullHttpResponse convert(String dataToSend) {

        log.debug("Sending data [{}]", dataToSend);

        ByteBuf msgBuf = ByteBufUtils.create(serializer,
                MessageHeader.builder()
                        .command(Command.READ)
                        .build(),
                Message.<String>builder()
                        .payload(dataToSend)
                        .build());

        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, msgBuf);
//        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
//        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, msgBuf.readableBytes());
        HttpUtil.setContentLength(response, 8192 * 5);
        return response;
    }
}
