package by.mrj.it.client.longpolling;

import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.utils.DataUtils;
import by.mrj.it.BasicData;
import com.google.common.collect.Lists;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
        (classes = {
                StreamerClientConfiguration.class,
                StreamerPostTest.class
        }, properties = {
                "config/application-dev.yml", "spring.main.banner-mode=off"
        })
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
public class StreamerPostTest {

    @Autowired
    private ConnectionManager connectionManager;

    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

    private AtomicInteger userIdIncrement = new AtomicInteger();

    @BeforeEach
    public void before() {

        // init section
//        assertThat(topicService.createTopic("First")).isNotNull();
    }

    @Test
    public void longPolling_post() throws InterruptedException {
        ServerChannelHolder channel = createChannel();

        long k = 0;
        while (true) {
            channel.send(Lists.newArrayList(
                    DataUtils.createNewData("First", "UU-ID" + k, BasicData.builder()
                            .id(0)
                            .name("base")
                            .uuid("UU-ID" + k++)
                            .build())));

            Thread.sleep(10_000);
        }

//        channel.closeFutureSync();
    }

    private ServerChannelHolder createChannel() {
        ConnectionInfo connectionInfo = ConnectionInfo.from(ConnectionType.HTTP_LP, null, host, port, "login-POST");

        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.connect(connectionInfo);
        log.info("Connection created. [{}]", serverChannelHolderSingle.blockingGet());

        log.debug("Sending WS Connect message...");
        ServerChannelHolder channel = connectionManager.findChannel(connectionInfo);

        assertThat(channel).isNotNull();
        assertThat(channel.rawChannel()).isNotNull();
        return channel;
    }
}
