package by.mrj.server.data;


import by.mrj.common.domain.data.BaseObject;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.listener.MapListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataProvider {

    /**
     * Returns all available data from {@param topic}.
     * @param maxSize
     * @return
     */
    List<BaseObject> getAllForUser(String clientId, int maxSize);

    Set<String> getKeysForTopic(String topicName);

    List<BaseObject> getAllForTopic(String clientId, String topicName, int maxSize);

    void putAll(String topicName, List<BaseObject> baseObjects);

    void addSubscription(String clientId, String subscription);

    void saveToMultiMap(String mapName, String key, Set<String> values);

    <K, V> void removeFromMultiMap(String mapName, Map<K, List<V>> entries);

    <K, V> void removeFromMultiMap(String mapName, K key, Collection<V> values);

    String registerListener(String mapName, MapListener listener, boolean includeValue);

    String registerListener(String mapName, EntryListener listener, boolean includeValue);

    Set<String> getAllClientsForSub(String topicName);
}
