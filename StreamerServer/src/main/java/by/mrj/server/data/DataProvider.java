package by.mrj.server.data;


import by.mrj.common.domain.data.BaseObject;
import by.mrj.server.data.domain.DataToSend;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.map.listener.MapListener;
import com.hazelcast.ringbuffer.Ringbuffer;

import java.io.Serializable;
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

    Set<Long> getKeysForTopic(String topicName);

    IAtomicLong getSequence(String name);

    void markAsSent(Long sequence);

    boolean wasSent(Long sequence);

    Map<String, Collection<Long>> getAllUuids(String clientId);

    Collection<BaseObject> get(String topicName, Set<Long> ids);

    List<BaseObject> getAllForTopic(String clientId, String topicName, int maxSize);

    Collection<Long> getAllIdFor(String clientId, String topicName);

    void putAll(String topicName, List<BaseObject> baseObjects);

    void addSubscription(String clientId, String subscription);

    Set<String> getAllClientsForSub(String topicName);

//    Ringbuffer<DataToSend> getRingBuffer(String ringBufName);

    <T> Ringbuffer<T> getRingBuffer(String ringBufName, Class<T> clazz);
}
