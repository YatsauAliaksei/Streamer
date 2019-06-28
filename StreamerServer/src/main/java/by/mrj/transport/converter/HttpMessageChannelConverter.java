package by.mrj.transport.converter;

import by.mrj.domain.Command;
import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import by.mrj.serialization.DataSerializer;
import by.mrj.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpMessageChannelConverter implements MessageChannelConverter<String, FullHttpResponse> {

    private final DataSerializer serializer;

    @Override
    public FullHttpResponse convert(String dataToSend) {

        ByteBuf msgBuf = ByteBufUtils.create(serializer,
                MessageHeader.builder()
                        .command(Command.READ)
                        .build(),
                Message.<String>builder()
                        .payload(dataToSend)
                        .build());

        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.ACCEPTED, msgBuf);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, msgBuf.readableBytes());
        return response;
    }
}
