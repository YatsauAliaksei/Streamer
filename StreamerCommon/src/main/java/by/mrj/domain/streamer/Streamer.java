package by.mrj.domain.streamer;

import by.mrj.domain.Message;

import java.io.Serializable;
import java.util.List;


public interface Streamer<T extends Serializable> {
    /**
     * Streams data to clients
     */
    void stream(List<Message<T>> messages);
}
