package by.mrj.common.utils;

import by.mrj.common.domain.Command;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.serialization.json.JsonJackson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.util.List;

public class ByteBufUtils {

    public static ByteBuf create(DataSerializer serializer, MessageHeader messageHeader, Message<?> msg) {

        ByteBuf header = Unpooled.buffer();
        // todo: support not only to String serialization
        header.writeCharSequence((String) serializer.serialize(messageHeader), CharsetUtil.UTF_8);

        ByteBuf body = Unpooled.buffer();
        // todo: support not only to String serialization
        body.writeCharSequence((String) serializer.serialize(msg.getPayload()), CharsetUtil.UTF_8);

        ByteBuf message = Unpooled.wrappedBuffer(2,
                Unpooled.buffer(4, 4).writeInt(messageHeader.getCommand().ordinal()), // header size
//                header,
                body
        );

        return message;
    }

    public static ByteBuf createPost(List<BaseObject> postData) {

        ByteBuf msg = Unpooled.buffer();
        // todo: support not only to String serialization
        msg.writeInt(Command.POST.ordinal());
        msg.writeCharSequence(JsonJackson.toJson(postData), CharsetUtil.UTF_8);

        return msg;
    }

    public static ByteBuf createRead(List<BaseObject> baseObj) {
        ByteBuf msg = Unpooled.buffer();
        msg.writeCharSequence(JsonJackson.toJson(baseObj), CharsetUtil.UTF_8);
        return msg;
    }

    public static ByteBuf create(String data) {
        ByteBuf msg = Unpooled.buffer();
        msg.writeCharSequence(data, CharsetUtil.UTF_8);
        return msg;
    }
}
