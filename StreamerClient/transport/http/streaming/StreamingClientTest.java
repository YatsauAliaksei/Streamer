package by.mrj.client.transport.http.streaming;

import by.mrj.domain.Command;
import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Benchmark)
@Slf4j
public class StreamingClientTest {

    private static StreamingClientChannelFactory streamingClient = new StreamingClientChannelFactory();
    private static final AtomicInteger userIdIncrement = new AtomicInteger();

    @BeforeAll
    @Setup(Level.Invocation)
    public static void before() throws InterruptedException {

//        log.info("Creating channel...");
        streamingClient.createChannel(group);

//        log.info("Channel created");

        Thread.sleep(500L); // could be done using Future/Listener way
    }

    @Test
    @Benchmark
    public void send() throws InterruptedException {
        log.debug("Sending benchmark message...");

        streamingClient.send(
                Message.<String>builder()
                        .payload("hs_user" + userIdIncrement.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

        streamingClient.closeFutureSync();
    }
}