package by.mrj.server.data;


import by.mrj.common.domain.data.BaseObject;
import by.mrj.server.data.domain.DataToSend;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ILock;
import com.hazelcast.map.listener.MapListener;
import com.hazelcast.ringbuffer.Ringbuffer;

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

    IAtomicLong getSequence(String name);

    void markAsSent(Long sequence);

    boolean wasSent(Long sequence);

    Map<String, Collection<String>> getAllUuids(String clientId);

    Collection<BaseObject> get(String topicName, Set<String> uuids);

    List<BaseObject> getAllForTopic(String clientId, String topicName, int maxSize);

    Collection<String> getAllIdFor(String clientId, String topicName);

    void putAll(String topicName, List<BaseObject> baseObjects);

    void addSubscription(String clientId, String subscription);

    void saveToMultiMap(String mapName, String key, Set<String> values);

    <K, V> void removeFromMultiMap(String mapName, Map<K, List<V>> entries);

    <K, V> void removeFromMultiMap(String mapName, K key, Collection<V> values);

    String registerListener(String mapName, MapListener listener, boolean includeValue);

    String registerListener(String mapName, EntryListener listener, boolean includeValue);

    Set<String> getAllClientsForSub(String topicName);

    boolean tryLock(String lockName);

    Ringbuffer<DataToSend> getRingBuffer(String ringBufName);

    ILock getLock(String lockName);

    void unlock(String lockName);
}
