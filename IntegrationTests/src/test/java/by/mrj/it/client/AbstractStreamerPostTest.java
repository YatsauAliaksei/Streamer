package by.mrj.it.client;

import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.utils.DataUtils;
import by.mrj.it.BasicData;
import io.reactivex.Single;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
public abstract class AbstractStreamerPostTest {

    @Autowired
    private ConnectionManager connectionManager;

    @Getter
    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Getter
    @Value("${streamer.host}")
    private String host; // todo: hosts

    @Test
    public void post() throws InterruptedException {
        ServerChannelHolder channel = createChannel();

        log.info("Posting {} batches with size {} using {}", batches(), batchSize(), connectionType());

        int batches = batches();
        while (batches-- > 0) {
            List<BaseObject> data = createData(batchSize());

            log.debug("Created {} objects", data.size());

            channel.post(data);

            log.info("Posted {} objects.", data.size());

            Thread.sleep(timeout());
        }

        log.info("Data sent. Total: {}", batches() * batchSize());

        channel.rawChannel().close();
    }

    protected int timeout() {
        return 2000;
    }

    protected abstract int batches();

    protected abstract int batchSize();

    protected abstract ConnectionType connectionType();

    public List<BaseObject> createData(int batchSize) {
        long now = Instant.now().toEpochMilli();
        return IntStream.range(0, batchSize)
                .boxed()
                .map(i -> new BaseObject(null, "First", 0, null, String.valueOf(now))
//                        BasicData.builder()
//                                .key("k1")
//                                .value("D-" + id++)
//                                .created(Instant.now().toEpochMilli())
//                                .build())
                ).collect(Collectors.toList());
    }

    protected abstract ConnectionInfo getConnectionInfo();

    private ServerChannelHolder createChannel() {
        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.connect(getConnectionInfo());
        ServerChannelHolder channel = serverChannelHolderSingle.blockingGet();
        log.info("Connection created. [{}]", channel);

        log.debug("Sending connect message...");
//        ServerChannelHolder channel = connectionManager.findChannel(connectionInfo);

        assertThat(channel).isNotNull();
        assertThat(channel.rawChannel()).isNotNull();
        return channel;
    }
}
