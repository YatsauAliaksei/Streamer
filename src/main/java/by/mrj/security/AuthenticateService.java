package by.mrj.security;

import by.mrj.domain.client.ClientConnection;

public interface AuthenticateService {
    boolean isAuthenticated(ClientConnection client);
}
