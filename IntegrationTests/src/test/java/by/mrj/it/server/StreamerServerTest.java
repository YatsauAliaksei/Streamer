package by.mrj.it.server;

import by.mrj.server.config.streamer.StreamerListenerConfiguration;
import by.mrj.server.topic.TopicService;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.util.ClientStateListener;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
        (classes = {
                StreamerListenerConfiguration.class,
                StreamerServerTest.class
        }, properties = {
                "config/application-dev.yml", "spring.main.banner-mode=off"
        })
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
public class StreamerServerTest {

    @Autowired
    private TopicService topicService;
    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private ClientConfig hzClientConfig;

    @BeforeEach
    public void after() {

        // init section
        assertThat(topicService.createTopic("First")).isNotNull();
    }

    @Test
    public void serverRun() throws InterruptedException {
        ClientStateListener lifecycleListener = new ClientStateListener(hzClientConfig);
        hazelcastInstance.getLifecycleService().addLifecycleListener(lifecycleListener);

        lifecycleListener.awaitConnected();
    }

    @Test
    public void clearance() {
        // clear everything
        for (DistributedObject distributedObject : hazelcastInstance.getDistributedObjects()) {
            log.info("Destroying HZ object [{}] type [{}]", distributedObject.getName(), distributedObject.getServiceName());

            distributedObject.destroy();
        }
    }
}
