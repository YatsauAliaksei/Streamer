package by.mrj.it;

import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.Command;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.Point;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.utils.DataUtils;
import by.mrj.server.config.streamer.StreamerListenerConfiguration;
import by.mrj.server.topic.TopicService;
import com.google.common.collect.Lists;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
        (classes = {
                StreamerClientConfiguration.class,
                StreamerListenerConfiguration.class,
                StreamerConnectionTypesTest.class
        }, properties = {
                "config/application-dev.yml", "spring.main.banner-mode=off"
        })
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
public class StreamerConnectionTypesTest {

    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private TopicService topicService;
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

    private AtomicInteger userIdIncrement = new AtomicInteger();

    @BeforeEach
    public void before() {
        // clear everything
        for (DistributedObject distributedObject : hazelcastInstance.getDistributedObjects()) {
            log.info("Destroying HZ object [{}] type [{}]", distributedObject.getName(), distributedObject.getServiceName());

            distributedObject.destroy();
        }

        // init section
        assertThat(topicService.createTopic("First")).isNotNull();
    }

    @Test
    public void httpStreaming() {
        ConnectionInfo connectionInfo = ConnectionInfo.from(ConnectionType.HTTP_STREAMING, null, host, port);

        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.connect(connectionInfo);
        log.info("Connection created. [{}]", serverChannelHolderSingle.blockingGet());

        log.debug("Sending WS Connect message...");
        ServerChannelHolder channel = connectionManager.findChannel(connectionInfo);

        assertThat(channel).isNotNull();
        assertThat(channel.rawChannel()).isNotNull();

        channel.send(
                Message.<Point>builder()
                        .payload(new Point("First", 0, 0))
                        .build(),

                MessageHeader
                        .builder()
                        .command(Command.READ_ALL)
                        .build());

        channel.closeFutureSync();
    }

    @Test
    public void longPolling() {
        ConnectionInfo connectionInfo = ConnectionInfo.from(ConnectionType.HTTP_LP, null, host, port);

        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.connect(connectionInfo);
        log.info("Connection created. [{}]", serverChannelHolderSingle.blockingGet());

        log.debug("Sending WS Connect message...");
        ServerChannelHolder channel = connectionManager.findChannel(connectionInfo);

        assertThat(channel).isNotNull();
        assertThat(channel.rawChannel()).isNotNull();

        channel.send(
                Message.<String>builder()
                        .payload("lp_user" + userIdIncrement.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.READ_ALL)
                        .build());

//        channel.closeFutureSync();
    }

    @Test
    public void webSocket_read() {
        ServerChannelHolder channel = getServerChannelHolder();


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

        CompletableFuture.runAsync(() -> {
            try {
                webSocket_post();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).join();

        channel.closeFutureSync();
    }

    @Test
    public void webSocket_post() throws InterruptedException {
        ServerChannelHolder channel = getServerChannelHolder();

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

    private ServerChannelHolder getServerChannelHolder() {
        ConnectionInfo connectionInfo = ConnectionInfo.from(ConnectionType.WS, null, host, port);

        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.connect(connectionInfo);
        log.info("Connection created. [{}]", serverChannelHolderSingle.blockingGet());

        log.debug("Sending WS Connect message...");
        ServerChannelHolder channel = connectionManager.findChannel(connectionInfo);

        assertThat(channel).isNotNull();
        assertThat(channel.rawChannel()).isNotNull();
        return channel;
    }
}
