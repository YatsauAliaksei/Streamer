package by.mrj.server.service.register;

import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static by.mrj.common.domain.client.DataClient.DUMMY;


@Slf4j
@RequiredArgsConstructor
public class InMemoryClientRegister implements ClientRegister {

    // todo: that model supports only 1 at time client connection.
    private ConcurrentMap<String, DataClient> register = new ConcurrentHashMap<>();
    private final List<NewClientRegistrationListener> clientRegistrationListener;
//    private final DataProvider dataProvider;

    @Override
    public void register(DataClient dataClient) {
        String clientId = dataClient.getId();
        DataClient prev = register.put(clientId, dataClient);

        dataClient.getStreamingChannel()
                .getChannel()
                .closeFuture()
                .addListener(future -> {
                    // todo: remove listener if DC was updated
                    log.info("Removing closed client from Register [{}]", clientId);

                    register.remove(clientId);
                });

        if (prev == null) {
            log.debug("New client registration [{}]", dataClient);

            for (NewClientRegistrationListener registrationListener : clientRegistrationListener) {
                registrationListener.handleNewRegistration(dataClient);
            }
        } else {
            log.debug("Client registration refreshed [{}]", dataClient);

            closeChannel(prev);
        }
    }

    @Override
    public void unregister(String clientId) {
        // todo:
//        DataClient dc = register.remove(clientId);
    }

    @Override
    public DataClient findBy(String id) {
        return register.getOrDefault(id.toUpperCase(), DUMMY);
    }

    private void closeChannel(DataClient prev) {
        ClientChannel streamingChannel = prev.getStreamingChannel();
        if (streamingChannel != null) {
            streamingChannel.close();
        }
    }
}
