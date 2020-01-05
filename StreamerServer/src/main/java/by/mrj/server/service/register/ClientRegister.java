package by.mrj.server.service.register;


import by.mrj.common.domain.client.DataClient;

public interface ClientRegister {

    /**
     * Persists {@param clientConnection} to register
     * If client already registered. Old connection details will be changed to new registration.
     */
    void register(DataClient dataClient);

    void unregister(String clientId);

    DataClient findBy(String id);
}
