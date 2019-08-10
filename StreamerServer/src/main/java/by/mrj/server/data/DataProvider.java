package by.mrj.server.data;


import by.mrj.common.domain.streamer.Topic;

import java.util.List;

public interface DataProvider<T> {

    List<T> getAll(Topic topic);
}
