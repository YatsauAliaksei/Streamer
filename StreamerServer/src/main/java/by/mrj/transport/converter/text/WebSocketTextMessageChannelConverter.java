package by.mrj.transport.converter.text;

import by.mrj.transport.converter.MessageChannelConverter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketTextMessageChannelConverter implements MessageChannelConverter<String, TextWebSocketFrame> {

    @Override
    public TextWebSocketFrame convert(String dataToSend) {
        return new TextWebSocketFrame(dataToSend);
    }
}
