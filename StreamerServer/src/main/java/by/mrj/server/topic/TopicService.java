package by.mrj.server.topic;

import by.mrj.common.domain.streamer.Topic;
import io.netty.buffer.ByteBuf;

public interface TopicService {

    Topic createTopic(ByteBuf msgBody);

    Topic createTopic(String topicName);

    Topic getTopic(String name);
}
