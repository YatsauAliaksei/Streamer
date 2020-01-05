package by.mrj.server.hz.listener;

import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import by.mrj.server.data.domain.DataUpdate;
import by.mrj.server.service.DecisionService;
import by.mrj.server.service.MapService;
import by.mrj.server.service.sender.strategy.EventBasedRegister;
import by.mrj.server.service.sender.strategy.PreSendBuffer;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionToIdsListener implements EntryAddedListener<String, String> {

    private final DataProvider dataProvider;
    private final DecisionService clusterMemberService;
    private final PreSendBuffer preSendBuffer;
    private final EventBasedRegister eventBasedRegister;
    private final MapService mapService;

    @Override
    public void entryAdded(final EntryEvent<String, String> event) {

        String key = event.getKey();
        String id = event.getValue();

        log.debug("New entry [{}] added to [{}]", id, key);

        String[] arr = HazelcastDataProvider.getClientIdTopicFromKey(key);
        String clientId = arr[0];
        String topicName = arr[1];

        if (!clusterMemberService.shouldIProcess("operation type", clientId)) {
            log.info("Seems not my job...");

            return;
        }

//        preSendBuffer.add(new DataUpdate(id, clientId, topicName));

        eventBasedRegister.eventBased(clientId);
    }

/*    @Override
    // todo: Basically means object was removed from Topic and therefore should be removed at clients sides as well
    // we can't use it for removing from client operation as it will cause cycle. We remove entries after send op at #entryAdded method.
    public void entryRemoved(EntryEvent<String, String> event) {
        if (log.isDebugEnabled()) {
            String key = event.getKey();

            log.debug("Removed obj [{}] from [{}]", event.getValue(), key);
        }
    }

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        entryRemoved(event);
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        log.debug("Update event for Subs -> ids listener");
    }

    @Override
    public void mapCleared(MapEvent event) {

    }

    @Override
    public void mapEvicted(MapEvent event) {

    }*/

    public String register() {
        log.warn("Registering SubscriptionToIds listener.");

        return mapService.registerListener(HzConstants.Maps.SUBSCRIPTION_TO_IDS, this, true);
    }
}
