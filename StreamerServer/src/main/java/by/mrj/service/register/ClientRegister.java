package by.mrj.service.register;

import by.mrj.domain.client.DataClient;

public interface ClientRegister {

    /**
     * Persists {@param clientConnection} to register
     * If client already registered. Old connection details will be changed to new registration.
     */
    void register(DataClient dataClient);

    DataClient findBy(String id);
}
