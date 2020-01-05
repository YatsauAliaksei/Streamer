package by.mrj.server.controller;

import by.mrj.common.domain.Command;
import by.mrj.common.domain.Point;
import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import by.mrj.server.security.SecurityUtils;
import by.mrj.server.service.post.PostOperationService;
import by.mrj.server.service.read.ReadOperationService;
import by.mrj.server.service.register.ClientRegister;
import by.mrj.server.service.subscription.SubscriptionService;
import by.mrj.server.topic.TopicService;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamerController implements CommandListener {

    private final ClientRegister clientRegister;
    private final ReadOperationService readOperationService;
    private final PostOperationService postOperationService;
    private final SubscriptionService subscriptionService;
    private final TopicService topicService;

    @Override
    public void processRequest(Object msg, ClientChannel streamChannel) {
    }

    @Override
    public void processRequest(Command command, ByteBuf msgBody, ClientChannel streamChannel) {
//        MessageHeader header = dataDeserializer.deserialize(msgHeader, MessageHeader.class);
        log.debug("Command received [{}]", command);

        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new UnsupportedOperationException("Not logged in user detected.")); // fixme: special Exception type

        clientRegistration(DataClient.builder()
                .id(currentUserLogin.toUpperCase())
                .streamingChannel(streamChannel)
                .build());

        // todo: limit processing time
        switch (command) {

            case POST:
                postOperationService.post(msgBody);
                break;

            case READ_SPECIFIC:
                readOperationService.read(msgBody);
                break;

            case READ_ALL:
                readOperationService.readAll();
                break;

            case SUBSCRIBE:
                subscriptionService.subscribe(msgBody);
                break;

            case UNSUBSCRIBE:
                subscriptionService.unsubscribe(msgBody);
                break;

            case CREATE_TOPIC:
                topicService.createTopic(msgBody);
                break;

            default:
                throw new UnsupportedOperationException();
                // close connection if unrecognized command
        }

        log.debug("{} operation processed.", command);
    }

    @Override
    public void clientRegistration(DataClient dataClient) {
        log.debug("Received [{}]", dataClient);

        clientRegister.register(dataClient);
    }

    @Override
    public void getDataFromPoint(Point point) {

    }

    @Override
    public void getDataAtPoints(List<Point> points) {

    }

    private void closeConnection(ClientChannel streamingChannel) {
        // TODO: Close channel
    }
}
