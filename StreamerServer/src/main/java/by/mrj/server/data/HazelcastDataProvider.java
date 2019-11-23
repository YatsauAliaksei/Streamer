package by.mrj.server.data;

import by.mrj.common.domain.data.BaseObject;
import com.google.common.collect.Sets;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.map.listener.MapListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HazelcastDataProvider implements DataProvider {

    private final HazelcastInstance hazelcastInstance;

    /**
     * @param clientId
     * @param maxSize - returned size limit // todo: not implemented yet
     * @return - all available {@link BaseObject} for subscription {@param clientId}
     */
    @Override
    public List<BaseObject> getAllForUser(String clientId, int maxSize) {

        MultiMap<String, String> userToSubs = hazelcastInstance.getMultiMap(HzConstants.Maps.USER_TO_SUBSCRIPTION);
        Collection<String> subs = Optional.ofNullable(userToSubs.get(clientId)).orElse(Collections.emptyList());

        if (subs.isEmpty()) {
            return Collections.emptyList();
        }

        MultiMap<String, String> subToIds = hazelcastInstance.getMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS);

        return subs.stream()
                .flatMap(topicName ->
                        hazelcastInstance.<String, BaseObject>getMap(topicName)
                                .getAll(Sets.newHashSet(subToIds.get(createSubsToIdsKey(clientId, topicName))))
                                .values()
                                .stream()
                ).collect(Collectors.toList());
    }

    /**
     * Same as {@link  HazelcastDataProvider#getAllForUser} but returns data only for specific {@param topicName}
     */
    @Override
    public List<BaseObject> getAllForTopic(String clientId, String topicName, int maxSize) {

        MultiMap<String, String> userToSubs = hazelcastInstance.getMultiMap(HzConstants.Maps.USER_TO_SUBSCRIPTION);
        Collection<String> subs = Optional.ofNullable(userToSubs.get(clientId)).orElse(Collections.emptyList());

        if (!subs.contains(topicName)) {
            log.warn("Active subscription for [{}] not found.", topicName);
            return Collections.emptyList();
        }

        MultiMap<String, String> subToIds = hazelcastInstance.getMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS);
        Collection<String> ids = subToIds.get(createSubsToIdsKey(clientId, topicName));

        if (ids.isEmpty()) {
            log.info("Nothing to send for client [{}] from [{}].", clientId, topicName);

            return Collections.emptyList();
        }

        IMap<String, BaseObject> cache = hazelcastInstance.getMap(topicName);
        Map<String, BaseObject> result = cache.getAll(Sets.newHashSet(ids));

        return new ArrayList<>(result.values());
    }

    /**
     *
     * @param topicName
     * @param baseObjects
     */
    @Override
    public void putAll(String topicName, List<BaseObject> baseObjects) {
        if (baseObjects.isEmpty()) {
            log.debug("Nothing to put. Empty list.");
            return;
        }

        // todo: those messages should be correlated to publisher, aka userID
        log.debug("Putting to [{}] {} objects", topicName, baseObjects.size());

        IMap<Object, Object> cache = hazelcastInstance.getMap(topicName);

        cache.putAll(baseObjects.stream()
                .peek(o -> {
                    log.debug("putting to {} k:{} v:{}", topicName, o.getUuid(), o);
                })
                .collect(Collectors.toMap(BaseObject::getUuid, Function.identity())));

        log.debug("{} objects added to topic [{}]", baseObjects.size(), topicName);
    }

    @Override
    public void addSubscription(String clientId, String subscription) {
        log.debug("Creating subscription [{}] for [{}]", subscription, clientId);

        MultiMap<String, String> map = hazelcastInstance.getMultiMap(HzConstants.Maps.USER_TO_SUBSCRIPTION);
        map.put(clientId, subscription);

        log.debug("Subscription [{}] created for [{}]", subscription, clientId);
    }

    /**
     * Creates MultiMap if doesn't exist
     */
    @Override
    public void saveToMultiMap(String mapName, String key, Set<String> values) {
        log.debug("Putting values to [{}] K:[{}] V:[{}]", mapName, key, values);

        MultiMap<String, String> map = hazelcastInstance.getMultiMap(mapName);

        values.forEach(v -> map.put(key, v));
    }

    @Override
    public <K, V> void removeFromMultiMap(String mapName, Map<K, List<V>> entries) {
        if (entries.isEmpty()) {
            log.info("Nothing to remove. Empty entries");
        }

        for (Map.Entry<K, List<V>> entry : entries.entrySet()) {
            removeFromMultiMap(mapName, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public <K, V> void removeFromMultiMap(String mapName, K key, Collection<V> values) {
        log.debug("Removing from [{}] v:[{}] for key [{}]", mapName, values, key);

        MultiMap<K, V> multiMap = hazelcastInstance.getMultiMap(mapName);

        for (V v : values) {
            Collection<V> vs = multiMap.get(key);

            multiMap.remove(key, v);
        }
    }

    @Override
    public String registerListener(String mapName, MapListener listener, boolean includeValue) {
        return hazelcastInstance.getMap(mapName).addEntryListener(listener, includeValue);
    }

    @Override
    public String registerListener(String mapName, EntryListener listener, boolean includeValue) {
        return hazelcastInstance.getMultiMap(mapName).addEntryListener(listener, includeValue);
    }

    @Override
    public Set<String> getAllClientsForSub(String topicName) {
        MultiMap<String, String> map = hazelcastInstance.getMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_USER);

        return Sets.newHashSet(map.get(topicName));
    }

    @Override
    public Set<String> getKeysForTopic(String topicName) {
        return hazelcastInstance.<String, BaseObject>getMap(topicName).keySet();
    }

    public static String createSubsToIdsKey(String clientId, String topicName) {
        return clientId + "_" + topicName;
    }

    public static String getClientIdFromKey(String key) {
        return key.split("_")[0];
    }
}
