package by.mrj.server.service.read;

import by.mrj.common.domain.Message;
import by.mrj.common.domain.Point;
import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.streamer.Topic;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import by.mrj.server.security.SecurityUtils;
import by.mrj.server.service.register.ClientRegister;
import by.mrj.server.service.sender.BasicClientSender;
import by.mrj.server.topic.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadOperationService {

    private final DataDeserializer dataDeserializer;
    private final DataProvider dataProvider;
    private final ClientRegister clientRegister;
    private final TopicService topicService;
    private final BasicClientSender basicClientSender;

    // todo: probably only needed to fix state if something goes wrong
    public void read(Object msgBody) {
        log.debug("Read message received [{}]", msgBody);

        Message<Point> pointMsg = dataDeserializer.deserializeMessage(msgBody, Point.class);

        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow((() -> new IllegalStateException("Unauthorized user"))); // fixme: should not ever happen here. Do we really need Optional here

        Point point = pointMsg.getPayload();

        String topicName = point.getTopic();

        DataClient client = clientRegister.findBy(currentUserLogin);
        Topic topic = client.getTopics().get(topicName);

        if (topic == null) {
            topic = topicService.getTopic(topicName);
            if (topic == null) {
                // should we have auto creation option + flag? This might be interesting in some cases. So far throwing Exception
                throw new IllegalStateException("Topic [" + topicName + "] was not found");
            }

            client.getTopics().put(topicName, topic);
        }


//        topic.readFrom(point.getSeqNumber(), point.getMaxDataSize());

//        clientRegistration(dataClient);

//        streamChannel.writeAndFlush("Connection succeeded");
//        break;

    }

    public void readAll() {

        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow((() -> new IllegalStateException("Unauthorized user"))); // fixme: should not ever happen here. Do we really need Optional here

        Map<String, List<String>> sentUuids = basicClientSender.sendTo(currentUserLogin, () -> dataProvider.getAllForUser(currentUserLogin, 0),
                (tn) -> HazelcastDataProvider.createSubsToIdsKey(currentUserLogin, tn.toUpperCase()));

        dataProvider.removeFromMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS, sentUuids);
    }
}
