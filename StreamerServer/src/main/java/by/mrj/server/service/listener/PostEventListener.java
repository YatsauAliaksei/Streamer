package by.mrj.server.service.listener;

import by.mrj.server.data.DataProvider;
import by.mrj.server.data.domain.Subscription;
import by.mrj.server.data.event.PostDataEvent;
import by.mrj.server.job.RingBufferEventRegister;
import by.mrj.server.service.data.ListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener implements ApplicationListener<PostDataEvent> {

    private final DataProvider dataProvider;
    private final ListService listService;
    private final RingBufferEventRegister ringBufferEventRegister;

    @Override
    public void onApplicationEvent(PostDataEvent event) {
        Map<String, List<Integer>> topicToIds = event.getTopicToIds();

        if (log.isDebugEnabled()) {
            log.debug("Creating subsToIds for topics [{}]", topicToIds.keySet());
        }

        createSubsToIds(topicToIds);
    }

    private void createSubsToIds(Map<String, List<Integer>> topicToIds) {

        for (Map.Entry<String, List<Integer>> entry : topicToIds.entrySet()) {
            String topicName = entry.getKey();
            List<Integer> values = entry.getValue();

            Set<String> clientIds = dataProvider.getAllClientsForSub(topicName);

            clientIds.forEach(clientId -> {
                Subscription subscription = new Subscription(clientId, topicName);

                log.trace("Subs to id updated for [{}] ids [{}]", subscription, values);

                listService.add(subscription.mapName(), values);

                ringBufferEventRegister.register(clientId);
            });
        }
    }
}
