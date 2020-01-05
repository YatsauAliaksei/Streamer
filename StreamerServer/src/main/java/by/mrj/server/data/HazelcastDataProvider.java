package by.mrj.server.data;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.server.data.domain.Subscription;
import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.ringbuffer.Ringbuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
     * @param maxSize  - returned size limit // todo: not implemented yet
     * @return - all available {@link BaseObject} for subscription {@param clientId}
     */
    @Override
    public List<BaseObject> getAllForUser(String clientId, int maxSize) {
        log.debug("Loading {} objects for {}", maxSize, clientId);

        MultiMap<String, String> userToSubs = hazelcastInstance.getMultiMap(HzConstants.Maps.USER_TO_SUBSCRIPTION);
        Collection<String> subs = Optional.ofNullable(userToSubs.get(clientId)).orElse(Collections.emptyList());

        if (subs.isEmpty()) {
            return Collections.emptyList();
        }

//        MultiMap<String, String> subToIds = hazelcastInstance.getMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS);
        List<BaseObject> data = new ArrayList<>(maxSize == Integer.MAX_VALUE ? 10 : maxSize);

        for (String topic : subs) {
            Subscription subscription = new Subscription(clientId, topic);
//            IMap<Long, Long> map = hazelcastInstance.getMap(subscription.mapName());
            IList<Long> list = hazelcastInstance.getList(subscription.mapName());

            if (list.size() == 0) {
                return Collections.emptyList();
            }

            Set<Long> ids = new HashSet<>(list.size() > maxSize ? list.subList(0, maxSize) : list);

//            Set<Long> ids = map.keySet(new PagingPredicate(maxSize));
            maxSize -= ids.size();

            log.debug("Fetched [{}] ids. [{}] left", ids.size(), maxSize);

            Collection<BaseObject> baseObjects = get(topic, ids);
            data.addAll(baseObjects);

            if (maxSize == 0) {
                log.debug("Limit {} reached.", data.size());
                break;
            }
        }

        return data;

/*        MultiMap<String, String> subToIds = hazelcastInstance.getMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS);
//        log.info("Subs to ids: [{}]", subToIds.entrySet());
        log.info("Subs to ids size: [{}]", subToIds.size());

        if (subToIds.size() == 0) {
            log.info("No data found");

            return Collections.emptyList();
        }

        return subs.stream()
                .flatMap(topicName -> {
                    Collection<String> ids = subToIds.get(createSubsToIdsKey(clientId, topicName));
                    Set<String> keys = ids.stream()
                            .limit(maxSize == 0 ? Integer.MAX_VALUE : maxSize)
                            .collect(Collectors.toSet());

                    Collection<BaseObject> values = hazelcastInstance.<String, BaseObject>getMap(topicName)
                            .getAll(keys)
                            .values();

                    log.info("Values loaded [{}]", values.size());

                    return values.stream();
                })
                .collect(Collectors.toList());*/
    }

    @Override
    public IAtomicLong getSequence(String name) {
        return hazelcastInstance.getAtomicLong(name);
    }

    @Override
    public void markAsSent(Long sequence) {
        hazelcastInstance.getSet("Sent_RB_Seq").add(sequence);
    }

    @Override
    public boolean wasSent(Long sequence) {
        return hazelcastInstance.getSet("Sent_RB_Seq").contains(sequence);
    }

    @Override
    public Map<String, Collection<Long>> getAllUuids(String clientId) {
        MultiMap<String, String> userToSubs = hazelcastInstance.getMultiMap(HzConstants.Maps.USER_TO_SUBSCRIPTION);
        Collection<String> subs = Optional.ofNullable(userToSubs.get(clientId)).orElse(Collections.emptyList());

        if (subs.isEmpty()) {
            return new HashMap<>();
        }

        MultiMap<String, Long> subToIds = hazelcastInstance.getMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS);
        log.debug("Subs to ids size: [{}]", subToIds.size());

        if (subToIds.size() == 0) {
            log.debug("No data found");

            return new HashMap<>();
        }

        return subs.stream()
                .collect(Collectors.toMap(Function.identity(),
                        topicName -> subToIds.get(createSubsToIdsKey(clientId, topicName))));
    }

    @Override
    public Collection<BaseObject> get(String topicName, Set<Long> ids) {
        log.debug("Fetching for [{}] ids [{}]", topicName, ids.size());

        IMap<Long, BaseObject> map = hazelcastInstance.getMap(topicName);

        return Optional.ofNullable(map.getAll(ids))
                .orElse(new HashMap<>())
                .values();
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

    @Override
    public Collection<Long> getAllIdFor(String clientId, String topicName) {
        MultiMap<String, Long> subToIds = hazelcastInstance.getMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS);

        return subToIds.get(createSubsToIdsKey(clientId, topicName));
    }

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
                .collect(Collectors.toMap(BaseObject::getId, Function.identity())));

        log.debug("{} objects added to topic [{}]", baseObjects.size(), topicName);
    }

    @Override
    public void addSubscription(String clientId, String subscription) {
        log.debug("Creating subscription [{}] for [{}]", subscription, clientId);

        MultiMap<String, String> map = hazelcastInstance.getMultiMap(HzConstants.Maps.USER_TO_SUBSCRIPTION);
        map.put(clientId, subscription);
    }

    @Override
    public Set<String> getAllClientsForSub(String topicName) {
        log.debug("Taking all clients subscribed to [{}]", topicName);

        MultiMap<String, String> map = hazelcastInstance.getMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_USER);

        Set<String> clients = Sets.newHashSet(map.get(topicName));

        log.debug("Received [{}] clients", clients.size());

        return clients;
    }

    @Override
    public Set<Long> getKeysForTopic(String topicName) {
        return hazelcastInstance.<Long, BaseObject>getMap(topicName).keySet();
    }

    @Override
    public <T> Ringbuffer<T> getRingBuffer(String ringBufName, Class<T> clazz) {
        return hazelcastInstance.getRingbuffer(ringBufName);
    }

    public static String createSubsToIdsKey(String clientId, String topicName) {
        return clientId + "_" + topicName;
    }

    public static String getClientIdFromKey(String key) {
        return key.split("_")[0];
    }

    public static String[] getClientIdTopicFromKey(String key) {
        return key.split("_");
    }
}
