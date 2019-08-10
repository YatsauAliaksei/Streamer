package by.mrj.client.transport;

import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import io.netty.channel.Channel;
import lombok.SneakyThrows;

public interface ServerChannel {
    Channel getChannel();

    void send(Message<?> msg, MessageHeader messageHeader);

    @SneakyThrows
    void closeFutureSync();
}
