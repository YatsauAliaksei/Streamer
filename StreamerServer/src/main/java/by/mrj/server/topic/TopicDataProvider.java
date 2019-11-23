package by.mrj.server.topic;

import by.mrj.common.domain.streamer.Topic;
import by.mrj.server.data.HzConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicDataProvider {

    private final HazelcastInstance hazelcastInstance;

    /**
     * Creates Map if doesn't exist
     */
    public Topic createTopic(String topicName) {
        IMap<String, Object> map = hazelcastInstance.getMap(HzConstants.Maps.TOPICS);

        String tn = topicName.toUpperCase();

        Object o = map.get(tn);
        if (o != null) {
            log.debug("Found existing topic. Returning it.");

            return new GenericTopic(tn);
        }

        map.put(tn, "stub"); // todo: stub

        log.info("Topic [{}] created", tn);

        return new GenericTopic(tn);
    }

    public Topic getTopic(String topicName) {
        IMap<String, Object> map = hazelcastInstance.getMap(HzConstants.Maps.TOPICS);

        String tn = topicName.toUpperCase();
        if (map.containsKey(tn)) {
            return new GenericTopic(tn);
        }

        return null;
    }

}
