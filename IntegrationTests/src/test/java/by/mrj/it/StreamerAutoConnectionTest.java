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
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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
                StreamerAutoConnectionTest.class
        }, properties = {
                "config/application-dev.yml", "spring.main.banner-mode=off"
        })
@ActiveProfiles("dev")
@Configuration
@ExtendWith(SpringExtension.class)
public class StreamerAutoConnectionTest {

    @Autowired
    private ConnectionManager connectionManager;
    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

    private AtomicInteger userIdIncrement = new AtomicInteger();

    @Test
    public void autoConnect() throws InterruptedException {
        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.autoConnect();
        log.info("Connection created. [{}]", serverChannelHolderSingle.blockingGet());

        log.debug("Sending WS Connect message...");

        ConnectionInfo connectionInfo = ConnectionInfo.from(ConnectionType.WS, null, host, port);

        ServerChannelHolder channel = connectionManager.findChannel(connectionInfo);

        assertThat(channel).isNotNull();
        assertThat(channel.rawChannel()).isNotNull();

        channel.send(
                Message.<String>builder()
                        .payload("auto_user" + userIdIncrement.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.READ)
                        .build());

//        channel.closeFutureSync();
    }
}
