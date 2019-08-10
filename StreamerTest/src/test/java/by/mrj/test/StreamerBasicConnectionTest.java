package by.mrj.test;

import by.mrj.StreamerClientApp;
import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.Command;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.server.config.AsyncConfiguration;
import by.mrj.server.config.streamer.StreamerListenerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest(classes = {
        StreamerListenerConfiguration.class,
//        StreamerBasicConnectionTest.PauseConfiguration.class,
//        AsyncConfiguration.class,
//        StreamerClientConfiguration.class,
        StreamerBasicConnectionTest.class
})
@ActiveProfiles("dev")
@Configuration
@ExtendWith(SpringExtension.class)
public class StreamerBasicConnectionTest {

    @Configuration
    public class PauseConfiguration {

        @Bean
        public String nothing() throws InterruptedException {
            Thread.sleep(500);
            return "";
        }
    }

    @Autowired
    private ConnectionManager connectionManager;
    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

    private AtomicInteger userIdIncrementWS = new AtomicInteger();

    @EventListener(ApplicationReadyEvent.class)
    public void createChannels() throws InterruptedException {
        StreamerClientApp.main(new String[0]);
    }

    @Test
    public void autoConnect() {
        log.debug("Sending WS Connect message...");

        ServerChannelHolder channel = connectionManager
                .findChannel(ConnectionInfo.from(ConnectionType.WS, null, host, port));

        assertThat(channel).isNotNull();

        channel.send(
                Message.<String>builder()
                        .payload("ws_user" + userIdIncrementWS.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.READ)
                        .build());

        channel.closeFutureSync();
    }
}
