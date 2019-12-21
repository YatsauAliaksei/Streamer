package by.mrj.client.transport.websocket;

import by.mrj.client.transport.ServerChannel;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ToString
public class WebSocketServerChannel implements ServerChannel {

    @Getter
    private final Channel channel;
    private final DataSerializer dataSerializer;
    @Getter
    private final SimpleChannelInboundHandler<?> handler;

    @Override
    public ChannelFuture send(Message<?> msg, MessageHeader messageHeader) {
        log.debug("Sending msg [{}]. Header [{}]", msg, messageHeader);

        ByteBuf msgBuf = ByteBufUtils.create(dataSerializer, messageHeader, msg);

        WebSocketFrame frame = new BinaryWebSocketFrame(msgBuf);
        ChannelFuture channelFuture = channel.writeAndFlush(frame);

        // todo: make common
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                log.error("Error occurred sending message. [{}]", msg);
            }
        });

        return channelFuture;

//        log.debug("Msg have been sent [{}]", frame.content().toString(CharsetUtil.UTF_8));
    }

    @Override
    public ChannelFuture send(List<BaseObject> postData) {
        log.info("Posting data [{}]", postData);

        ByteBuf msgBuf = ByteBufUtils.createPost(postData);

        WebSocketFrame frame = new BinaryWebSocketFrame(msgBuf);
        ChannelFuture channelFuture = channel.writeAndFlush(frame);

        // todo: make common
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                log.error("Error occurred sending message. [{}]", postData);
            }
        });

        return channelFuture;
    }

    @Override
    @SneakyThrows
    public void closeFutureSync() {
        channel.closeFuture().sync();
    }
}
