package by.mrj.server.topic;

import by.mrj.common.domain.streamer.Topic;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.server.data.DataProvider;
import by.mrj.server.hz.listener.NewTopicEntriesListener;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenericTopicService implements TopicService {

    private final DataProvider dataProvider;
    private final DataDeserializer dataDeserializer;
    private final TopicDataProvider topicDataProvider;
    private final NewTopicEntriesListener newTopicEntriesListener;

    @Override
    public Topic createTopic(ByteBuf msgBody) {
        String topicName = dataDeserializer.deserialize(msgBody.toString(CharsetUtil.UTF_8), String.class);

        return createTopic(topicName);
    }

    /**
     * Creates {@link Topic} with {@param topicName} name.
     * Creates
     */
    @Override
    public Topic createTopic(String topicName) {
        Topic topic = topicDataProvider.createTopic(topicName);

        dataProvider.registerListener(topic.getName(), newTopicEntriesListener, false);

        log.info("Registering Topic listener for [{}]", topic.getName());

        return topic;
    }

    @Override
    public Topic getTopic(String name) {
        return topicDataProvider.getTopic(name);
    }
}
