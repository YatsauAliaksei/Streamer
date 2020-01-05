package by.mrj.it.client;

import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.service.MessageConsumer;
import by.mrj.client.transport.ServerChannel;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.Statistic;
import by.mrj.common.domain.client.ConnectionInfo;
import com.google.common.collect.Lists;
import io.reactivex.Single;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
public abstract class AbstractStreamerReadTest {

    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private MessageConsumer messageConsumer;

    @Value("${streamer.port}")
    @Getter
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    @Getter
    private String host; // todo: hosts

    public ServerChannel read(ConnectionInfo connectionInfo) {

        log.debug("Starting READ {}", connectionInfo);

        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.connect(connectionInfo);
        ServerChannelHolder channelHolder = serverChannelHolderSingle.blockingGet();
        log.debug("Connection created. [{}]", channelHolder);

        log.debug("Sending subscribe message...");

        String topic = "First";
        String login = connectionInfo.getLogin();

        channelHolder.subscribe(Lists.newArrayList(topic)).syncUninterruptibly();
        log.debug("[{}] subscribed to [{}]", login, topic);

        channelHolder.readAll();
        log.debug("Reading all for [{}]", login);

        return channelHolder.getChannel();
    }

    @Test
    @SneakyThrows
    public void readMultiple() {
        List<ConnectionInfo> connectionInfos = connectionInfo();

        log.info("Starting creation of [{}] readers", connectionInfos.size());

        ServerChannel[] block = new ServerChannel[1];

        for (ConnectionInfo connectionInfo : connectionInfos) {
            new Thread(() -> {
                ServerChannel serverChannel = read(connectionInfo);
                if (block[0] == null) {
                    block[0] = serverChannel;
                }
                serverChannel.closeFutureSync();
            }).start();

//            Thread.sleep(20);
        }

        log.info("All {} consumers ready...", connectionInfos.size());

        Thread.sleep(1_000);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {

            try {

                Statistic statistic = messageConsumer.statistics();
                log.info("Stats: {}", statistic);

            } catch (Exception e) {
                log.error("Error while getting stats.", e);
            }

        }, 10L, 10L, TimeUnit.SECONDS);

        log.info("Blocking...");

        block[0].closeFutureSync();
    }

    protected abstract List<ConnectionInfo> connectionInfo();
}
