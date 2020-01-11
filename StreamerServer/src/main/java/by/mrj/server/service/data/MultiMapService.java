package by.mrj.server.service.data;

import com.hazelcast.core.EntryListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MultiMapService {

    void saveToMultiMap(String mapName, String key, Set<? extends Serializable> values);

    <K, V> void removeFromMultiMap(String mapName, Map<K, List<V>> entries);

    <K, V> void removeFromMultiMap(String mapName, K key, Collection<V> values);

    String registerListener(String mapName, EntryListener listener, boolean includeValue);
}
