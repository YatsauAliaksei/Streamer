package by.mrj.server.service.subscription;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.domain.streamer.Topic;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HzConstants;
import by.mrj.server.data.domain.Subscription;
import by.mrj.server.security.SecurityUtils;
import by.mrj.server.service.data.ListService;
import by.mrj.server.service.data.MultiMapService;
import by.mrj.server.service.sender.BasicClientSender;
import by.mrj.server.topic.TopicService;
import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionService {

    private final DataDeserializer dataDeserializer;
    private final DataProvider dataProvider;
    private final MultiMapService multiMapService;
    private final HazelcastInstance hazelcastInstance;
    private final TopicService topicService;
    private final BasicClientSender basicClientSender;
    private final ListService listService;

    public void subscribe(ByteBuf msgBody) {

        String[] topicsToSubscribe =
                dataDeserializer.deserialize(msgBody.toString(CharsetUtil.UTF_8), String[].class);

        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow((() -> new IllegalStateException("Unauthorized user"))); // fixme: should not ever happen here. Do we really need Optional here

        if (log.isInfoEnabled()) {
            log.debug("Subscribing client [{}] to topics [{}]", currentUserLogin, Arrays.toString(topicsToSubscribe));
        }

        for (String sub : topicsToSubscribe) {
            Topic topic = topicService.getTopic(sub);

            if (topic == null) {
                log.warn("No topic [{}] found to subscribe. Skipping...", sub);
                continue;
            }

            String topicName = topic.getName();
            dataProvider.addSubscription(currentUserLogin, topicName);
            multiMapService.saveToMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_USER, topicName, Sets.newHashSet(currentUserLogin));

            Subscription subscription = new Subscription(currentUserLogin, topicName);

            Set<Long> values = dataProvider.getKeysForTopic(topicName);

            log.trace("Subs to id updated for [{}] ids [{}]", subscription, values);

            listService.add(subscription.mapName(), values);
        }

        basicClientSender.send(currentUserLogin, new BaseObject[]{
                BaseObject.builder().payload("Subscribed").build()
        });
    }

    // todo: fixme
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
}
