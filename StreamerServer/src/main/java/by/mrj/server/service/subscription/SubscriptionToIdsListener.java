package by.mrj.server.service.subscription;

import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import by.mrj.server.service.sender.BasicClientSender;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionToIdsListener implements EntryListener<String, String> {

    private final DataProvider dataProvider;
    private final BasicClientSender basicClientSender;

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        String key = event.getKey();
        String id = event.getValue();

        log.debug("New entry [{}] added to [{}]", id, key);

        String clientId = HazelcastDataProvider.getClientIdFromKey(key);

        log.debug("Sending all to [{}] ", clientId);

        Map<String, List<String>> sentUuids = basicClientSender.sendTo(clientId, () -> dataProvider.getAllForUser(clientId, 0),
                (tn) -> HazelcastDataProvider.createSubsToIdsKey(clientId, tn.toUpperCase()));

        dataProvider.removeFromMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS, sentUuids);
    }

    @Override
    // todo: Basically means object was removed from Topic and there for should be removed at clients sides as well
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
//        entryAdded(event);
    }

    @Override
    public void mapCleared(MapEvent event) {

    }

    @Override
    public void mapEvicted(MapEvent event) {

    }

    public String register() {
        log.warn("Registering SubscriptionToIds listener.");

        return dataProvider.registerListener(HzConstants.Maps.SUBSCRIPTION_TO_IDS, this, true);
    }
}
