package by.mrj.server.hz.listener;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import com.google.common.collect.Sets;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewTopicEntriesListener implements EntryAddedListener<String, BaseObject>, EntryUpdatedListener<String, BaseObject> {

    private final DataProvider dataProvider;

    @Override
    public void entryAdded(EntryEvent<String, BaseObject> event) {
        log.trace("Topic Added. Entry event [{}] found", event);

        String topicName = (String) event.getSource();
        Set<String> clientIds =  dataProvider.getAllClientsForSub(topicName);

        clientIds.forEach(id -> {
            String subsToIdsKey = HazelcastDataProvider.createSubsToIdsKey(id, topicName);

            log.trace("Subs to id updated for [{}] id [{}]", subsToIdsKey, event.getKey());

            dataProvider.saveToMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS,
                    subsToIdsKey, Sets.newHashSet(event.getKey()));
        });

    }

    @Override
    public void entryUpdated(EntryEvent<String, BaseObject> event) {
        log.trace("Topic Updated. Entry event [{}] found", event);

        entryAdded(event);
    }
}
