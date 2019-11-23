package by.mrj.server.security;


import io.undertow.client.ClientConnection;

public interface AuthenticateService {
    boolean isAuthenticated(ClientConnection client);
}
