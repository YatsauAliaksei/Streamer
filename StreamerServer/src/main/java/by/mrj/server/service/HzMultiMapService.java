package by.mrj.server.service;

import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class HzMultiMapService implements MultiMapService {

    private final HazelcastInstance hazelcastInstance;

    /**
     * Creates MultiMap if doesn't exist
     */
    @Override
    public void saveToMultiMap(String mapName, String key, Set<? extends Serializable> values) {
        log.trace("Putting values to [{}] K:[{}] V:[{}]", mapName, key, values);

        MultiMap<String, Object> map = hazelcastInstance.getMultiMap(mapName);

        values.forEach(v -> map.put(key, v));
    }

    @Override
    public <K, V> void removeFromMultiMap(String mapName, Map<K, List<V>> entries) {
        if (entries.isEmpty()) {
            log.debug("Nothing to remove. Empty entries");
        }

        for (Map.Entry<K, List<V>> entry : entries.entrySet()) {
            removeFromMultiMap(mapName, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public <K, V> void removeFromMultiMap(String mapName, K key, Collection<V> values) {
        log.debug("Removing from [{}] s:[{}] for key [{}]", mapName, values.size(), key);

        MultiMap<K, V> multiMap = hazelcastInstance.getMultiMap(mapName);

        for (V v : values) {
//            Collection<V> vs = multiMap.get(key);

            multiMap.remove(key, v);
        }
    }

    @Override
    public String registerListener(String mapName, EntryListener listener, boolean includeValue) {
        return hazelcastInstance.getMultiMap(mapName).addEntryListener(listener, includeValue);
    }
}
