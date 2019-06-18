package by.mrj.domain;


import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@RequiredArgsConstructor
@Getter
@ToString
public class StreamingChannel {
    private final Channel channel;

    public void writeAndFlush(WebSocketFrame frame) {
        channel.writeAndFlush(frame);
    }
}
