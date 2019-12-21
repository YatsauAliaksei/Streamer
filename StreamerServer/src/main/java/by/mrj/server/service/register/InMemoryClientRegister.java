package by.mrj.server.service.register;

import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import by.mrj.server.data.DataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static by.mrj.common.domain.client.DataClient.DUMMY;


@Slf4j
@RequiredArgsConstructor
public class InMemoryClientRegister implements ClientRegister {

    private Map<String, DataClient> register = new ConcurrentHashMap<>();
    private final List<NewClientRegistrationListener> clientRegistrationListener;
//    private final DataProvider dataProvider;

    @Override
    public void register(DataClient dataClient) {
        DataClient prev = register.put(dataClient.getId(), dataClient);

//        dataProvider.

        dataClient.getStreamingChannel()
                .getChannel()
                .closeFuture()
                .addListener(future -> register.remove(dataClient.getId()));

        if (prev == null) {
            log.debug("New client registration [{}]", dataClient);

            for (NewClientRegistrationListener registrationListener : clientRegistrationListener) {
                registrationListener.handleNewRegistration(dataClient);
            }
        } else {
            log.debug("Client registration refreshed [{}]", dataClient);

            ClientChannel streamingChannel = prev.getStreamingChannel();
            if (streamingChannel != null) {
                streamingChannel.close();
            }
        }
    }

    @Override
    public DataClient findBy(String id) {
        return register.getOrDefault(id, DUMMY);
    }

}
