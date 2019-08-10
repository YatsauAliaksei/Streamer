package by.mrj.server.controller;

import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.Point;
import by.mrj.common.domain.client.DataClient;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.domain.client.channel.ClientChannel;
import by.mrj.server.service.register.ClientRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamerController implements CommandListener {

    private final DataDeserializer dataDeserializer;
    private final ClientRegister clientRegister;

    @Override
    public void processRequest(Object msg, ClientChannel streamChannel) {
    }

    @Override
    public void processRequest(String msgHeader, Object msgBody, ClientChannel streamChannel) {
        MessageHeader header = dataDeserializer.deserialize(msgHeader, MessageHeader.class);
        log.debug("Header received [{}]", header);

        // todo: limit processing time
        switch (header.getCommand()) {
            case POST:
                break;
            case READ:
                Message<String> msg = dataDeserializer.deserializeMessage(msgBody, String.class);

                log.debug("Connect message received [{}]", msg);

                String loginName = msg.getPayload();
                DataClient dataClient = DataClient.builder()
                        .loginName(loginName)
                        .streamingChannel(streamChannel)
                        .build();

                clientRegistration(dataClient);

                streamChannel.writeAndFlush("Connection succeeded");
                break;
            default:
                throw new UnsupportedOperationException();
                // close connection if unrecognized command
        }
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
