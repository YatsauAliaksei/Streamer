package by.mrj.server.service.register;

import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Slf4j
@RequiredArgsConstructor
public class InMemoryClientRegister implements ClientRegister {

    // todo: that model supports only 1 at time client connection.
    private ConcurrentMap<String, Set<DataClient>> register = new ConcurrentHashMap<>();
    private final List<NewClientRegistrationListener> clientRegistrationListener;

    @Override
    public void register(DataClient dataClient) {
        String clientId = dataClient.getId();

        Set<DataClient> registeredDCs = register.computeIfAbsent(clientId, k -> new HashSet<>());

        if (registeredDCs.contains(dataClient)) {
            log.debug("Same channel nothing to update");

            return;
        }

        log.debug("New client registration [{}]", dataClient);

        registeredDCs.add(dataClient);

        dataClient.getStreamingChannel().getChannel().closeFuture()
                .addListener(future -> {
                    log.info("Removing closed client from Register [{}]", clientId);

                    Set<DataClient> dataClients = register.get(clientId);
                    dataClients.remove(dataClient);
                });

        for (NewClientRegistrationListener registrationListener : clientRegistrationListener) {
            registrationListener.handleNewRegistration(dataClient);
        }
    }

    @Override
    public void unregister(String clientId) {
        // todo:
//        DataClient dc = register.remove(clientId);
    }

    @Override
    public Set<DataClient> findBy(String id) {
        return register.getOrDefault(id.toUpperCase(), new HashSet<>());
    }

    @Override
    public DataClient takeBest(String id) {
        Set<DataClient> dataClients = findBy(id);

        if (dataClients.isEmpty()) {
            return DataClient.DUMMY;
        }

        DataClient best = null;
        for (DataClient dataClient : dataClients) {
            if (best == null) {
                best = dataClient;
                continue;
            }

            // todo: change to function. Consider channel creation time.
            // so far WS treated as the best connection option if exist
            if (dataClient.getStreamingChannel().getConnectionInfo().getConnectionType() == ConnectionType.WS) {
                best = dataClient;
                break;
            }
        }

        return best;
    }

    private void closeChannel(DataClient prev) {
        ClientChannel streamingChannel = prev.getStreamingChannel();
        if (streamingChannel != null) {
            streamingChannel.close();
        }
    }
}
