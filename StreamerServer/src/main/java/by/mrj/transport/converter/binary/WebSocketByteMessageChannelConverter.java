package by.mrj.transport.converter.binary;

import by.mrj.transport.converter.MessageChannelConverter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class WebSocketByteMessageChannelConverter implements MessageChannelConverter<ByteBuf, BinaryWebSocketFrame> {

    @Override
    public BinaryWebSocketFrame convert(ByteBuf dataToSend) {
        return new BinaryWebSocketFrame(dataToSend);
    }
}
