package by.mrj.client.transport;

import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.data.BaseObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.SneakyThrows;

import java.util.List;

public interface ServerChannel {
    Channel getChannel();

    ChannelFuture send(Message<?> msg, MessageHeader messageHeader);

    ChannelFuture send(List<BaseObject> postData);

    @SneakyThrows
    void closeFutureSync();
}
