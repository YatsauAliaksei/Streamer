package by.mrj.security;

import by.mrj.domain.client.Client;
import by.mrj.streamer.Topic;

public interface AuthorizeService {

    boolean isAuthorized(Topic topic, Client client);
}
