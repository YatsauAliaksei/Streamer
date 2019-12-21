package by.mrj.common.domain.client.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;


public interface ClientChannel {

    void writeAndFlush(String toSend);

    void writeAndFlush(ByteBuf toSend);

    void write(String toSend);

    void write(ByteBuf toSend);

    void flush();

    Channel getChannel();

    default void close() {

    }
}
