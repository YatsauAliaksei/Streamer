package by.mrj.it.client;

import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import com.google.common.collect.Lists;
import io.reactivex.Single;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


@Slf4j
public abstract class AbstractStreamerReadTest {

    @Autowired
    private ConnectionManager connectionManager;

    @Value("${streamer.port}")
    @Getter
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    @Getter
    private String host; // todo: hosts

    @Test
    public void read() {

        log.info("Starting READ {}", connectionInfo());

        Single<ServerChannelHolder> serverChannelHolderSingle = connectionManager.connect(connectionInfo());
        ServerChannelHolder channelHolder = serverChannelHolderSingle.blockingGet();
        log.info("Connection created. [{}]", channelHolder);

        log.debug("Sending connect message...");
//        ServerChannelHolder channel = connectionManager.findChannel(connectionInfo);

//        assertThat(channel).isNotNull();
//        assertThat(channel.rawChannel()).isNotNull();

        channelHolder.subscribe(Lists.newArrayList("First")).syncUninterruptibly();
        log.info("Subscribed");

        if (connectionInfo().getConnectionType() != ConnectionType.WS) {
            channelHolder.readAll();
        }

/*        channel.send(
                Message.<String[]>builder()
                        .payload(new String[]{"First"})
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.SUBSCRIBE)
                        .build()).syncUninterruptibly();*/


        channelHolder.getChannel().closeFutureSync();
    }

    protected abstract ConnectionInfo connectionInfo();
}
