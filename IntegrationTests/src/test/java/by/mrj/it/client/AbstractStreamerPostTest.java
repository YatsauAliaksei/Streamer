package by.mrj.it.client;

import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.utils.DataUtils;
import by.mrj.it.BasicData;
import com.google.common.collect.Lists;
import io.reactivex.Single;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
public abstract class AbstractStreamerPostTest {

    @Autowired
    private ConnectionManager connectionManager;

    @Value("${streamer.port}")
    @Getter
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    @Getter
    private String host; // todo: hosts

    @Test
    public void post() throws InterruptedException {
        ServerChannelHolder channel = createChannel();

        log.info("Posting {} batches with size {} using {}", batches(), batchSize(), connectionType());

        int batches = batches();
        while (batches-- > 0) {
            List<BaseObject> data = createData(batchSize());

            channel.post(Lists.newArrayList(data));
            Thread.sleep(10);
        }

        log.info("Data sent");

//        channel.closeFutureSync();
    }

    protected abstract int batches();

    protected abstract int batchSize();

    protected abstract ConnectionType connectionType();

    public List<BaseObject> createData(int batchSize) {
        return IntStream.range(0, batchSize)
                .boxed()
                .map(i -> DataUtils.createNewData("First", "UU-ID" + i,
                        BasicData.builder()
                                .id(0)
                                .name("base")
                                .uuid("UU-ID-" + i))
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
