package by.mrj;

import by.mrj.controller.StreamerController;
import by.mrj.domain.Message;
import by.mrj.domain.StreamingChannel;
import by.mrj.serialization.json.JsonJackson;
import by.mrj.service.register.InMemoryClientRegister;
import by.mrj.transport.websocket.server.WebSocketServer;
import com.google.common.collect.Lists;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
class BootstrapTest {

    @Test
    public void serverListener() {
        StreamerController streamerController = getStreamerController();

        Bootstrap bootstrap = Bootstrap.builder()
                .portListener(new WebSocketServer())
                .commandListener(streamerController)
                .build();

        bootstrap.run();
    }

    private StreamerController getStreamerController() {
        List<Message<String>> messages = testData(1);

        return StreamerController.builder()
                .clientRegister(InMemoryClientRegister.builder()
                        .clientRegistrationListener(Lists.newArrayList((dataClient) -> {

                            StreamingChannel streamingChannel = dataClient.getStreamingChannel();
                            messages.stream()
                                    .peek(m -> log.debug("Sending message to client [{}]", m))
                                    .map(JsonJackson::toJson)
                                    .map(TextWebSocketFrame::new)
                                    .forEach(streamingChannel::writeAndFlush);

                            log.info("Client registered [{}]", dataClient.getLoginName());

                            streamingChannel.getChannel().close();
                        }))
                        .build())
                .deserializer(new JsonJackson())
                .build();
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