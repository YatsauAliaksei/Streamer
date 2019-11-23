package by.mrj.server.service.post;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.domain.streamer.Topic;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.server.data.DataProvider;
import by.mrj.server.topic.TopicDataProvider;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class PostOperationService {

    private final DataProvider dataProvider;
    private final TopicDataProvider topicDataProvider;
    private final DataDeserializer dataDeserializer;

    public void post(ByteBuf msgBody) {

        List<BaseObject> postData =
                Arrays.asList(dataDeserializer.deserialize(msgBody.toString(CharsetUtil.UTF_8), BaseObject[].class));

        log.debug("Posting data [{}]", postData);

        Map<String, List<BaseObject>> topicToData = postData.stream()
                .collect(Collectors.groupingBy(pd -> pd.getTopic().toUpperCase()));

        for (Map.Entry<String, List<BaseObject>> entry : topicToData.entrySet()) {

//            List<BaseObject> baseObjects = entry.getValue().stream()
//                    .map(BaseObject::getPayload)
//                    .collect(Collectors.toList());

            String topicName = entry.getKey();
            List<BaseObject> baseObjects = entry.getValue();
            Topic topic = topicDataProvider.getTopic(topicName);

            if (topic == null) {
                log.warn("No Topic [{}] found to post data [{}]", topicName.toUpperCase(), baseObjects);
                continue;
            }

            dataProvider.putAll(topic.getName(), baseObjects);
        }
    }
}
