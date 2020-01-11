package by.mrj.server.service.data;

import com.hazelcast.map.listener.MapListener;

import java.util.List;
import java.util.function.Function;

public interface MapService {

    String registerListener(String mapName, MapListener listener, boolean includeValue);

    <K, V> void put(String mapName, List<V> values, Function<V, K> keyExtractor);

    void remove(String s, Comparable[] ids);
}
