package by.mrj.service.register;

import by.mrj.domain.client.DataClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InMemoryClientRegister implements ClientRegister {

    @Builder.Default
    private Map<String, DataClient> register = new ConcurrentHashMap<>();
    @NotNull
    private List<NewClientRegistrationListener> clientRegistrationListener;

    @Override
    public DataClient register(DataClient dataClient) {
        if (register.put(dataClient.getLoginName(), dataClient) == null) {
            for (NewClientRegistrationListener registrationListener : clientRegistrationListener) {
                registrationListener.handleNewRegistration(dataClient);
            }
        }

        return dataClient;
    }
}
