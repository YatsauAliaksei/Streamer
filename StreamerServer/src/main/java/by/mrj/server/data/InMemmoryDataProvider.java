package by.mrj.server.data;

import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.domain.streamer.Topic;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//@Component
public class InMemmoryDataProvider {//} implements DataProvider {

    private Map<String, List<BaseObject>> topics = new HashMap<>();

//    @Override
    public List<BaseObject> getAll(DataClient dataClient, int maxSize) {
        return null;
    }

//    @Override
    public Set<String> getKeysForTopic(String topicName) {
        return null;
    }

//    @Override
    public List<BaseObject> getAllForTopic(String dataClient, String topicName, int maxSize) {
        return null;
    }

//    @Override
    public void putAll(String topicName, List<BaseObject> baseObjects) {
        topics.computeIfPresent(topicName, (s, bo) -> {
            bo.addAll(baseObjects);
            return bo;
        });

        topics.putIfAbsent(topicName, baseObjects);
    }
}
