package by.mrj.data;

import by.mrj.domain.streamer.Topic;

import java.util.List;

public interface DataProvider<T> {

    List<T> getAll(Topic topic);
}
