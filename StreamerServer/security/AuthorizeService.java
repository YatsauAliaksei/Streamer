package by.mrj.security;

import by.mrj.domain.client.DataClient;
import by.mrj.domain.streamer.Topic;

public interface AuthorizeService {

    boolean isAuthorized(Topic topic, DataClient client);
}
