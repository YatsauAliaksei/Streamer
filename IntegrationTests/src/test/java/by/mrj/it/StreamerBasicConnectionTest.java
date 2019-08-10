package by.mrj.it;

import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.Command;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.server.config.streamer.StreamerListenerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
        (classes = {
                StreamerClientConfiguration.class,
                StreamerListenerConfiguration.class,
                StreamerBasicConnectionTest.class
        }, properties = "config/application-dev.yml")
@ActiveProfiles("dev")
@Configuration
@ExtendWith(SpringExtension.class)
public class StreamerBasicConnectionTest {

    @Autowired
    private ConnectionManager connectionManager;
    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

    private AtomicInteger userIdIncrementWS = new AtomicInteger();

    @BeforeEach
    public void before() {
        ServerChannelHolder serverChannelHolder = connectionManager.autoConnect();
        log.info("Connection created. [{}]", serverChannelHolder);
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
