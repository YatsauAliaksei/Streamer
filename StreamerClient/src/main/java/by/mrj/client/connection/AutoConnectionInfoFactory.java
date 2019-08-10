package by.mrj.client.connection;

import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class AutoConnectionInfoFactory implements Supplier<ConnectionInfo> {

    @Value("${streamer.port}")
    private Integer port; // todo: ports
    @Value("${streamer.host}")
    private String host; // todo: hosts

    @Override
    public ConnectionInfo get() {
        return ConnectionInfo.from(ConnectionType.WS, null, host, port);
    }
}
