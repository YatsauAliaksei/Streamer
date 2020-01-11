package by.mrj.server.service.post;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.domain.streamer.Topic;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.server.data.DataProvider;
import by.mrj.server.data.event.PostDataEvent;
import by.mrj.server.topic.TopicDataProvider;
import com.hazelcast.core.IAtomicLong;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.var;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Log4j2
@Component
@RequiredArgsConstructor
public class PostOperationService {

    private final DataProvider dataProvider;
    private final TopicDataProvider topicDataProvider;
    private final DataDeserializer dataDeserializer;
    private final ApplicationEventPublisher publisher;

    public void post(ByteBuf msgBody) {

        log.debug("Post obj deserialization");

        BaseObject[] deserialize = dataDeserializer.deserialize(msgBody.toString(CharsetUtil.UTF_8), BaseObject[].class);

        log.debug("Deserialize {} objects", deserialize.length);

        List<BaseObject> postData =
                Arrays.asList(deserialize);

        log.debug("Posting data [{}]", postData);

        Map<String, List<BaseObject>> topicToData = postData.stream()
                .collect(Collectors.groupingBy(pd -> pd.getTopic().toUpperCase()));

        Set<String> topics = topicToData.keySet();

        log.debug("Taking sequence");

        Map<String, IAtomicLong> topicToSeq = topics.stream()
                .collect(Collectors.toMap(String::toUpperCase, (t) -> dataProvider.getSequence(t + "_SEQ")));

        Map<String, List<Long>> sentObjs = new HashMap<>();

        for (Map.Entry<String, List<BaseObject>> entry : topicToData.entrySet()) {

            String topicName = entry.getKey();
            List<BaseObject> baseObjects = entry.getValue();

            log.debug("Taking topic");

            Topic topic = topicDataProvider.getTopic(topicName);

            log.debug("Topic received");

            if (topic == null) {
                log.warn("No Topic [{}] found to post data [{}]", topicName, baseObjects);
                continue;
            }

            log.debug("Seq get");

            IAtomicLong seq = topicToSeq.get(topicName);
            int size = baseObjects.size();
            long finalSeq = seq.addAndGet(size);

            log.debug("Seq set & get");

            for (int i = 1; i <= baseObjects.size(); i++) {
                long id = finalSeq - size + i;
                var bo = baseObjects.get(i - 1);

                bo.setId(id);
            }

            dataProvider.putAll(topicName, baseObjects);

            List<Long> ids = baseObjects.stream()
                    .map(BaseObject::getId)
                    .collect(Collectors.toList());

            sentObjs.put(topicName, ids);
        }

        log.info("Posted {} objects", postData.size());

        // todo: not sure event based approach needed here
        publisher.publishEvent(new PostDataEvent(this, sentObjs));
    }
}
