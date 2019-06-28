package by.mrj.utils;

import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import by.mrj.serialization.DataSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class ByteBufUtils {

    public static ByteBuf create(DataSerializer serializer, MessageHeader messageHeader, Message<?> msg) {

        ByteBuf header = Unpooled.buffer();
        // todo: support not only to String serialization
        header.writeCharSequence((String) serializer.serialize(messageHeader), CharsetUtil.UTF_8);

        ByteBuf body = Unpooled.buffer();
        // todo: support not only to String serialization
        body.writeCharSequence((String) serializer.serialize(msg), CharsetUtil.UTF_8);

        ByteBuf message = Unpooled.wrappedBuffer(3,
                Unpooled.buffer(4, 4).writeInt(header.readableBytes()), // header size
                header,
                body
        );

        return message;
    }
}
