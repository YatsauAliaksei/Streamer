package by.mrj.client.transport.http.longpolling;

import by.mrj.domain.Command;
import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import by.mrj.client.transport.ServerChannelHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Benchmark)
@Slf4j
public class LongPollingHttpClientTest {

    private ServerChannelHolder
    private static LongPollingClient longPollingHttpClient = new LongPollingClient();
    private static final AtomicInteger userIdIncrement = new AtomicInteger();

    @BeforeAll
    @Setup(Level.Invocation)
    public static void before() throws InterruptedException {

//        log.info("Creating channel...");
        longPollingHttpClient.createChannel();

//        log.info("Channel created");

        Thread.sleep(500L); // could be done using Future/Listener way
    }

    @Test
    @Benchmark
    public void send() throws InterruptedException {
        log.debug("Sending benchmark message...");

        longPollingHttpClient.send(
                Message.<String>builder()
                        .payload("lp_user" + userIdIncrement.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

        longPollingHttpClient.closeFutureSync();
    }
}