package by.mrj.server.hz.listener;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import by.mrj.server.service.data.MultiMapService;
import by.mrj.server.service.merkletree.MerkleTreeService;
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
public class NewTopicEntriesListener implements EntryAddedListener<Long, BaseObject>, EntryUpdatedListener<Long, BaseObject> {

    private final DataProvider dataProvider;
    private final MultiMapService multiMapService;
    private final MerkleTreeService merkleTreeService;

    @Override
    public void entryAdded(EntryEvent<Long, BaseObject> event) {
        log.trace("Topic Added. Entry event [{}] found", event);


        String topicName = (String) event.getSource();

        merkleTreeService.set(topicName, event.getKey(), event.getValue().getHash());

/*        String topicName = (String) event.getSource();
        Set<String> clientIds = dataProvider.getAllClientsForSub(topicName);

        clientIds.forEach(id -> {
            String subsToIdsKey = HazelcastDataProvider.createSubsToIdsKey(id, topicName);

            log.trace("Subs to id updated for [{}] id [{}]", subsToIdsKey, event.getKey());

            multiMapService.saveToMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS,
                    subsToIdsKey, Sets.newHashSet(event.getKey()));
        });*/

    }

    @Override
    public void entryUpdated(EntryEvent<Long, BaseObject> event) {
        log.trace("Topic Updated. Entry event [{}] found", event);

        entryAdded(event);
    }
}
