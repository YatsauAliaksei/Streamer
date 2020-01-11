package by.mrj.server.service.register;


import by.mrj.common.domain.client.DataClient;

import java.util.Set;

public interface ClientRegister {

    /**
     * Persists {@param clientConnection} to register
     * If client already registered. Old connection details will be changed to new registration.
     */
    void register(DataClient dataClient);

    void unregister(String clientId);

    Set<DataClient> findBy(String id);

    DataClient takeBest(String id);
}
