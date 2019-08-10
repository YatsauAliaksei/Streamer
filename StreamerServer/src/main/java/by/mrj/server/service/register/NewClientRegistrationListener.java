package by.mrj.server.service.register;


import by.mrj.common.domain.client.DataClient;

public interface NewClientRegistrationListener {

    void handleNewRegistration(DataClient dataClient);
}
