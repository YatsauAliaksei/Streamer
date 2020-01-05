package by.mrj.client.transport.event;

import by.mrj.client.connection.ConnectionHolder;
import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResendService implements ApplicationListener<MessageReceivedEvent> {

    private final ConnectionHolder connectionHolder;

    private ConcurrentMap<ConnectionInfo, ServerChannelHolder> watched = new ConcurrentHashMap<>();
    @Setter
    private ScheduledExecutorService scheduledExecutorPool =
            Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setNameFormat("resend-scheduled-%d").build());

    @Override
    public void onApplicationEvent(MessageReceivedEvent event) {
        ConnectionInfo connectionInfo = event.getConnectionInfo();

        if (connectionInfo.getConnectionType().oneOf(ConnectionType.WS)) {
            return; // no need to resend for WS
        }

        ServerChannelHolder channel = connectionHolder.findChannel(connectionInfo);

        log.debug("Resending using [{}]", channel);
        channel.readAll();

/*        if (connectionInfo.getConnectionType().oneOf(ConnectionType.HTTP_STREAMING, ConnectionType.HTTP_LP)) {
            watched.put(connectionInfo, channel);

            scheduledExecutorPool.scheduleWithFixedDelay(() -> {

                if (channel.rawChannel().isActive()) {
                    channel.readAll();
                }

            }, 1_000L, 1_000L, TimeUnit.MILLISECONDS);
        }*/
    }
}
