package by.mrj.it;

import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.client.connection.ConnectionHolder;
import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.utils.DataUtils;
import by.mrj.server.config.streamer.StreamerListenerConfiguration;
import com.google.common.collect.Lists;
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
//    @Autowired
//    private ConnectionHolder connectionHolder;
    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

//    private AtomicInteger userIdIncrement = new AtomicInteger();

    @Test
    public void autoConnect_Read() {
        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.autoConnect();
        ServerChannelHolder channel = serverChannelHolderSingle.blockingGet();
        log.info("Connection created. [{}]", channel);

        ConnectionType connectionType = ConnectionType.WS;
        log.debug("Sending {} Connect message...", connectionType);

//        ConnectionInfo connectionInfo = ConnectionInfo.from(connectionType, null, host, port);

//        ServerChannelHolder channel = connectionHolder.findChannel(connectionInfo);

        assertThat(channel).isNotNull();
        assertThat(channel.rawChannel()).isNotNull();

/*        channel.send(
                Message.<String>builder()
                        .payload("auto_user" + userIdIncrement.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.READ_SPECIFIC)
                        .build());*/

        channel.subscribe(Lists.newArrayList("First")).syncUninterruptibly();
        channel.readAll();
/*
        channel.send(Lists.newArrayList(
                DataUtils.createNewData("topicName", "UU-ID", BasicData.builder()
                        .id(0)
                        .key("base")
                        .id("UU-ID")
                        .build())));*/

        channel.closeFutureSync();
    }
}
