package by.mrj.test.connection;

import by.mrj.server.StreamerServerApp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;


@Slf4j
@SpringBootTest(classes = {StreamerServerApp.class, HttpStreamingConnectionTest.class})
@ActiveProfiles("dev")
@Configuration
public class HttpStreamingConnectionTest {

/*    private static final StreamingClientChannelFactory streamingClient = new StreamingClientChannelFactory();

    private AtomicInteger userIdIncrementST = new AtomicInteger();

    @EventListener(ApplicationReadyEvent.class)
    public void createChannels() throws InterruptedException {
        log.info("Creating ST channel...");
        streamingClient.createChannel(group);

        log.info("Waiting...");
        Thread.sleep(500L); // could be done using Future/Listener way

        log.info("Channels created");
    }

    @Test
    public void sendST() {
        log.debug("Sending Streaming Connect message...");

        streamingClient.send(
                Message.<String>builder()
                        .payload("hs_user" + userIdIncrementST.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

        streamingClient.closeFutureSync();
    }*/
}
