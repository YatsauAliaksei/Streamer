package by.mrj.service.register;

import by.mrj.domain.client.DataClient;

public interface NewClientRegistrationListener {

    void handleNewRegistration(DataClient dataClient);
}
