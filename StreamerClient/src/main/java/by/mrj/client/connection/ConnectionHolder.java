package by.mrj.client.connection;

import by.mrj.client.transport.ServerChannelHolder;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionHolder {

    private ConcurrentMap<ConnectionInfo, ServerChannelHolder> connectionToChannel = new ConcurrentHashMap<>();

    public ServerChannelHolder put(ConnectionInfo connectionInfo, ServerChannelHolder serverChannelHolder) {
        return connectionToChannel.put(connectionInfo, serverChannelHolder);
    }

    public ServerChannelHolder remove(ConnectionInfo connectionInfo) {
        return connectionToChannel.remove(connectionInfo);
    }

    public ServerChannelHolder findChannel(ConnectionInfo connectionInfo) {
        return connectionToChannel.getOrDefault(connectionInfo, null);
    }

    public List<ServerChannelHolder> findByType(ConnectionType connectionType) {

        List<ServerChannelHolder> result = new ArrayList<>();

        for (Map.Entry<ConnectionInfo, ServerChannelHolder> entry : connectionToChannel.entrySet()) {
            if (entry.getKey().getConnectionType() == connectionType) {
                result.add(entry.getValue());
            }
        }

        return result;
    }
}
