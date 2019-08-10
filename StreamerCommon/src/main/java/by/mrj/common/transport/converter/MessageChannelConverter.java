package by.mrj.common.transport.converter;

import io.netty.buffer.ByteBuf;

public interface MessageChannelConverter<OUT> {

    OUT convert(String dataToSend);

    OUT convert(ByteBuf dataToSend);
}
