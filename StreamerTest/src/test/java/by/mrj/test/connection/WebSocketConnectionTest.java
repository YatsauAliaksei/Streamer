package by.mrj.test.connection;

import by.mrj.server.StreamerServerApp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;


@Slf4j
@SpringBootTest(classes = {StreamerServerApp.class, WebSocketConnectionTest.class})
@ActiveProfiles("dev")
@Configuration
public class WebSocketConnectionTest {

/*    private static final WebSocketClientChannelFactory webSocketClient = new WebSocketClientChannelFactory();

    private AtomicInteger userIdIncrementWS = new AtomicInteger();

    @EventListener(ApplicationReadyEvent.class)
    public void createChannels() throws InterruptedException {
        log.info("Creating WS channel...");
        webSocketClient.createChannel();

        log.info("Waiting...");
        Thread.sleep(500L); // could be done using Future/Listener way

        log.info("WS handshake sync");
        webSocketClient.getHandshakeFuture().sync();

        log.info("Channels created");
    }

    @Test
    public void sendWS() {
        log.debug("Sending WS Connect message...");

        webSocketClient.send(
                Message.<String>builder()
                        .payload("ws_user" + userIdIncrementWS.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

        webSocketClient.closeFutureSync();
    }*/
}
