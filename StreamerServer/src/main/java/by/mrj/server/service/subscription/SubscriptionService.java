package by.mrj.server.service.subscription;

import by.mrj.common.domain.streamer.Topic;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.server.data.DataProvider;
import by.mrj.server.security.SecurityUtils;
import by.mrj.server.data.HzConstants;
import by.mrj.server.topic.TopicDataProvider;
import by.mrj.server.topic.TopicService;
import com.google.common.collect.Sets;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionService {

    private final DataDeserializer dataDeserializer;
    private final DataProvider dataProvider;
    private final HazelcastInstance hazelcastInstance;
    private final TopicService topicService;

    public void subscribe(ByteBuf msgBody) {

        String[] topicsToSubscribe =
                dataDeserializer.deserialize(msgBody.toString(CharsetUtil.UTF_8), String[].class);

        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow((() -> new IllegalStateException("Unauthorized user"))); // fixme: should not ever happen here. Do we really need Optional here

        if (log.isInfoEnabled()) {
            log.info("Subscribing client [{}] to topics [{}]", currentUserLogin, Arrays.toString(topicsToSubscribe));
        }

        for (String sub : topicsToSubscribe) {
            Topic topic = topicService.getTopic(sub);

            if (topic == null) {
                log.warn("No topic [{}] found to subscribe. Skipping...", sub);
                continue;
            }

            dataProvider.addSubscription(currentUserLogin, topic.getName());
            dataProvider.saveToMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_USER, topic.getName(), Sets.newHashSet(currentUserLogin));
        }
    }

    public void unsubscribe(ByteBuf msgBody) {
        List<String> topicsToUnSubscribe =
                Arrays.asList(dataDeserializer.deserialize(msgBody.toString(CharsetUtil.UTF_8), String[].class));

        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow((() -> new IllegalStateException("Unauthorized user"))); // fixme: should not ever happen here. Do we really need Optional here

        log.info("Removing subscription for client [{}] from topics [{}]", currentUserLogin, topicsToUnSubscribe);

        IMap<String, Set<String>> userToSubs = hazelcastInstance.getMap(HzConstants.Maps.USER_TO_SUBSCRIPTION);

        // todo: remove Topic listeners on Unsubscribe

        userToSubs.executeOnKey(currentUserLogin, new EntryProcessor<String, Set<String>>() {
            @Override
            public Object process(Map.Entry<String, Set<String>> entry) {
                Set<String> subs = entry.getValue();
                subs.removeAll(topicsToUnSubscribe);
                entry.setValue(subs);
                return subs;
            }

            @Override
            public EntryBackupProcessor getBackupProcessor() {
                return null;
            }
        });
    }

    private void addTopicListeners(String[] topicsToSubscribe, IMap<String, Set<String>> userToSubs) {
        for (String topic : topicsToSubscribe) {
            IMap<Object, Object> map = hazelcastInstance.getMap(topic);

            map.addEntryListener((EntryAddedListener) event -> {

                log.debug("'Add' event at [{}] occurred", topic);

                updateUserSubs(userToSubs, topic, event);
            }, false);


            map.addEntryListener((EntryUpdatedListener) event -> {

                log.debug("'Update' event at [{}] occurred", topic);

                updateUserSubs(userToSubs, topic, event);
            }, false);
        }
    }

    private void updateUserSubs(IMap<String, Set<String>> userToSubs, String topic, EntryEvent event) {
        userToSubs.executeOnKey(topic, new EntryUpdateProcessor(topic, event));
    }

    @AllArgsConstructor
    private static class EntryUpdateProcessor implements EntryProcessor<String, Map<String, Set<String>>>, Serializable {

        private String topic;
        private EntryEvent event;

        @Override
        public Object process(Map.Entry<String, Map<String, Set<String>>> entry) {
            Map<String, Set<String>> value = entry.getValue();
            Set<String> ids = value.computeIfAbsent(topic, k -> new HashSet<>());
            ids.add((String) event.getKey());
            entry.setValue(value);

            return null;
        }

        @Override
        public EntryBackupProcessor getBackupProcessor() {
            return null;
        }
    }
}
