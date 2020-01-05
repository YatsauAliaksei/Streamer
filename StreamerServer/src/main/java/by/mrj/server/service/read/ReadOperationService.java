package by.mrj.server.service.read;

import by.mrj.common.domain.Message;
import by.mrj.common.domain.Point;
import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.streamer.Topic;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.server.job.RingBufferEventRegister;
import by.mrj.server.security.SecurityUtils;
import by.mrj.server.service.register.ClientRegister;
import by.mrj.server.service.sender.LockingSender;
import by.mrj.server.topic.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadOperationService {

    private final DataDeserializer dataDeserializer;
    private final ClientRegister clientRegister;
    private final TopicService topicService;
    private final LockingSender lockingSender;
    private final RingBufferEventRegister ringBufferEventRegister;

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
    }

    public void readAll() {
//        log.info("After getting connection it should start working from ");
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow((() -> new IllegalStateException("Unauthorized user"))); // fixme: should not ever happen here. Do we really need Optional here

        ringBufferEventRegister.register(currentUserLogin);
//        lockingSender.sendAndRemove(currentUserLogin);
    }
}
