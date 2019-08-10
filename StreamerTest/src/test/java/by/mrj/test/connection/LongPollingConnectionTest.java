package by.mrj.test.connection;

import by.mrj.server.StreamerServerApp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;


@Slf4j
@SpringBootTest(classes = {StreamerServerApp.class, LongPollingConnectionTest.class})
@ActiveProfiles("dev")
@Configuration
public class LongPollingConnectionTest {

/*    private static final LongPollingClient longPollingHttpClient = new LongPollingClient();

    private AtomicInteger userIdIncrementLP = new AtomicInteger();

    @EventListener(ApplicationReadyEvent.class)
    public void createChannels() throws InterruptedException {
        log.info("Creating LP channel...");
        longPollingHttpClient.createChannel();

        log.info("Waiting...");
        Thread.sleep(500L); // could be done using Future/Listener way

        log.info("Channels created");
    }

    @Test
    public void sendLP() {
        log.debug("Sending Long Polling Connect message...");

        longPollingHttpClient.send(
                Message.<String>builder()
                        .payload("lp_user" + userIdIncrementLP.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

        longPollingHttpClient.closeFutureSync();
    }*/
}
