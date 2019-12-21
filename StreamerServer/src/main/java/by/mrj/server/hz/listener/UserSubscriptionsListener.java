package by.mrj.server.hz.listener;

import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSubscriptionsListener implements EntryListener<String, String> {

    private final DataProvider dataProvider;

    @Override
    public void entryAdded(EntryEvent<String, String> event) {

        String clientId = event.getKey();
        String topic = event.getValue();

        log.debug("New subscription [{}] created for [{}]", topic, clientId);

        Set<String> keys = dataProvider.getKeysForTopic(topic);

        if (keys.isEmpty()) {
            log.info("No data found to update subs to id");
            return;
        }

        dataProvider.saveToMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS,
                HazelcastDataProvider.createSubsToIdsKey(clientId, topic), keys);

        log.debug("Subs to ids updated with [{}]", keys);
    }

    @Override
    public void entryRemoved(EntryEvent<String, String> event) {
        // todo: implement
    }

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        entryRemoved(event);
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        entryAdded(event);
    }

    @Override
    public void mapCleared(MapEvent event) {

    }

    @Override
    public void mapEvicted(MapEvent event) {

    }

    public String register() {
        log.warn("Registering User -> Subscription listener.");

        return dataProvider.registerListener(HzConstants.Maps.USER_TO_SUBSCRIPTION, this, true);
    }
}
