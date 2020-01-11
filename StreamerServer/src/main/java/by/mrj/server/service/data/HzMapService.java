package by.mrj.server.service.data;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.MapListener;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.QueryConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HzMapService implements MapService {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public String registerListener(String mapName, MapListener listener, boolean includeValue) {
        return hazelcastInstance.getMap(mapName).addEntryListener(listener, includeValue);
    }

    @Override
    public <K, V> void put(String mapName, List<V> values, Function<V, K> keyExtractor) {
        IMap<K, V> map = hazelcastInstance.getMap(mapName);

        Map<K, V> data = values.stream()
                .collect(Collectors.toMap(keyExtractor, Function.identity()));

        map.putAll(data);
    }

    @Override
    public void remove(String mapName, Comparable[] ids) {
        IMap<Object, Object> map = hazelcastInstance.getMap(mapName);

        log.debug("Removing {} from {}", ids.length, mapName);

        map.removeAll(Predicates.in(QueryConstants.KEY_ATTRIBUTE_NAME.value(), ids));
    }
}
