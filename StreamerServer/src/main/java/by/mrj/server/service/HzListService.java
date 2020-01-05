package by.mrj.server.service;

import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class HzListService implements ListService {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public void remove(String listName, Collection<?> ids) {
        hazelcastInstance.getList(listName).removeAll(ids);
    }

    @Override
    public void add(String listName, Collection<?> ids) {
        log.debug("Adding to [{}] list {} values", listName, ids.size());

        hazelcastInstance.getList(listName).addAll(ids);

        log.debug("Added to [{}] {} values", listName, ids.size());
    }
}
