package by.mrj.transport.converter.binary;

import by.mrj.transport.converter.MessageChannelConverter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpByteMessageChannelConverter implements MessageChannelConverter<ByteBuf, FullHttpResponse> {

    @Override
    public FullHttpResponse convert(ByteBuf dataToSend) {

        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, dataToSend);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8"); //  application/octet-stream
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, dataToSend.readableBytes());
        return response;
    }
}
