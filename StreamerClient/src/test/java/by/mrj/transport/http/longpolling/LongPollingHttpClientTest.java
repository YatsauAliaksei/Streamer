package by.mrj.transport.http.longpolling;

import by.mrj.domain.Command;
import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@State(Scope.Benchmark)
@Slf4j
public class LongPollingHttpClientTest {

    private static LongPollingHttpClient longPollingHttpClient = new LongPollingHttpClient();
    private AtomicInteger iterator = new AtomicInteger();

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
                        .payload("myLoginName-" + iterator.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

        longPollingHttpClient.closeFutureSync();
    }
}