package by.mrj.service.register;

import by.mrj.domain.client.DataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static by.mrj.domain.client.DataClient.DUMMY;

@Slf4j
@RequiredArgsConstructor
public class InMemoryClientRegister implements ClientRegister {

    private Map<String, DataClient> register = new ConcurrentHashMap<>();
    private final List<NewClientRegistrationListener> clientRegistrationListener;

    @Override
    public void register(DataClient dataClient) {
        if (register.put(dataClient.getLoginName(), dataClient) == null) {
            log.debug("New client registration [{}]", dataClient);

            for (NewClientRegistrationListener registrationListener : clientRegistrationListener) {
                registrationListener.handleNewRegistration(dataClient);
            }
        } else {
            log.debug("Client registration refreshed [{}]", dataClient);
        }
    }

    @Override
    public DataClient findBy(String id) {
        return register.getOrDefault(id, DUMMY);
    }

}
