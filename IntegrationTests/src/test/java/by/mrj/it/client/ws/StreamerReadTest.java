package by.mrj.it.client.ws;

import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.Command;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.client.ConnectionInfo;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
        (classes = {
                StreamerClientConfiguration.class,
                StreamerReadTest.class
        }, properties = {
                "config/application-dev.yml", "spring.main.banner-mode=off"
        })
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
public class StreamerReadTest {

    @Autowired
    private ConnectionManager connectionManager;

    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

    @Test
    public void webSocket_read() {
        ConnectionInfo connectionInfo = ConnectionInfo.from(ConnectionType.WS, null, host, port, "login-READ");

        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.connect(connectionInfo);
        log.info("Connection created. [{}]", serverChannelHolderSingle.blockingGet());

        log.debug("Sending WS Connect message...");
        ServerChannelHolder channel = connectionManager.findChannel(connectionInfo);

        assertThat(channel).isNotNull();
        assertThat(channel.rawChannel()).isNotNull();

        channel.send(
                Message.<String[]>builder()
                        .payload(new String[]{"First"})
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.SUBSCRIBE)
                        .build()).syncUninterruptibly();


        channel.send(
                Message.<String>builder()
                        .payload("Read all Opp")
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.READ_ALL)
                        .build());

        channel.closeFutureSync();
    }
}
