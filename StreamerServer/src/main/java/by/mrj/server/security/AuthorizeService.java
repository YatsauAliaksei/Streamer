package by.mrj.server.security;


import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.streamer.Topic;

public interface AuthorizeService {

    boolean isAuthorized(Topic topic, DataClient client);
}
