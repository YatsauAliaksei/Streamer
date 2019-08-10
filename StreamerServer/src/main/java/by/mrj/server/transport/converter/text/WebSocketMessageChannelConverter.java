package by.mrj.server.transport.converter.text;

import by.mrj.common.transport.converter.MessageChannelConverter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessageChannelConverter implements MessageChannelConverter<WebSocketFrame> {

    @Override
    public WebSocketFrame convert(String dataToSend) {
        return new TextWebSocketFrame(dataToSend);
    }

    @Override
    public WebSocketFrame convert(ByteBuf dataToSend) {
        return new BinaryWebSocketFrame(dataToSend);
    }
}
