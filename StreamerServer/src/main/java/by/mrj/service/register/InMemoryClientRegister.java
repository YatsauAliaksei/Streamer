package by.mrj.service.register;

import by.mrj.domain.client.DataClient;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class InMemoryClientRegister implements ClientRegister {

    private Map<String, DataClient> register = new ConcurrentHashMap<>();
    private final List<NewClientRegistrationListener> clientRegistrationListener;

    @Override
    public DataClient register(DataClient dataClient) {
        if (register.put(dataClient.getLoginName(), dataClient) == null) {
        }
            for (NewClientRegistrationListener registrationListener : clientRegistrationListener) {
                registrationListener.handleNewRegistration(dataClient);
            }

        return dataClient;
    }
}
