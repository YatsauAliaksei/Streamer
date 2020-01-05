package by.mrj.server.hz;

import by.mrj.server.hz.listener.SubscriptionToIdsListener;
import by.mrj.server.hz.listener.UserSubscriptionsListener;
import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class HzListenerService implements InitializingBean {

    private final HazelcastInstance hazelcastInstance;
//    private final SubscriptionToIdsListener subscriptionToIdsListener;
    private final UserSubscriptionsListener userSubscriptionsListener;

    private ConcurrentMap<String, Object> listeners = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        CompletableFuture.runAsync(() -> {

            while (true) {
                if (hazelcastInstance.getLifecycleService().isRunning()) {
                    log.info("HZ is running. . Registering listeners...");

                    String
//                            id = subscriptionToIdsListener.register();
//                    listeners.put(id, subscriptionToIdsListener);

                    id = userSubscriptionsListener.register();
                    listeners.put(id, userSubscriptionsListener);

                    log.info("Listeners registered successfully.");
                    break;
                }

                try {
                    log.debug("HZ not yet running. Waiting 1s");
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
