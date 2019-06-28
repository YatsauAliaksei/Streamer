package by.mrj;

import by.mrj.controller.StreamerController;
import by.mrj.domain.Message;
import by.mrj.domain.StreamingChannel;
import by.mrj.serialization.json.JsonJackson;
import by.mrj.service.register.InMemoryClientRegister;
import by.mrj.transport.websocket.server.WebSocketServer;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
class BootstrapTest {

    @Test
    public void serverListener() {
        StreamerController streamerController = getStreamerController();

        Bootstrap bootstrap = Bootstrap.builder()
                .portListener(new WebSocketServer(null))
                .commandListener(streamerController)
                .build();

        bootstrap.run();
    }

    private StreamerController getStreamerController() {
        List<Message<String>> messages = testData(1);

        return null;
/*        return StreamerController.builder()
                .clientRegister(InMemoryClientRegister.builder()
                        .clientRegistrationListener(Lists.newArrayList((dataClient) -> {

                            StreamingChannel streamingChannel = dataClient.getStreamingChannel();
                            messages.stream()
                                    .peek(m -> log.debug("Sending message to client [{}]", m))
                                    .map(JsonJackson::toJson)
                                    .forEach(streamingChannel::write);

                            log.info("Client registered [{}]", dataClient.getLoginName());

                            streamingChannel.flush();

                            streamingChannel.getChannel().close(); // in real world we shouldn't do that
                        }))
                        .build())
                .deserializer(new JsonJackson())
                .build();*/
    }

    private List<Message<String>> testData(int size) {
        return IntStream.range(0, size)
                .boxed()
                .map(k -> Message.<String>builder()
                        .payload("Hi-" + k)
                        .build())
                .collect(Collectors.toList());
    }
}