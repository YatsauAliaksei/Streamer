package by.mrj.client.transport.websocket;

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
public class WebSocketClientTest {

    private static WebSocketClientChannelFactory webSocketClient = new WebSocketClientChannelFactory();
    private static final AtomicInteger userIdIncrement = new AtomicInteger();

    @BeforeAll
    //    @Benchmark
//    @Fork(jvmArgsAppend = "-Xmx2g")
//    @BenchmarkMode(Mode.SingleShotTime)
//    @Threads(2)
    @Setup(Level.Invocation)
    public static void before() throws InterruptedException {

        log.debug("Creating channel...");
        webSocketClient.createChannel();
        Thread.sleep(500L);

        webSocketClient.getHandshakeFuture().sync();

        log.debug("Channel created");

    }

    @Test
    @Benchmark
    public void send() throws InterruptedException {
        log.debug("Sending benchmark message...");

        webSocketClient.send(
                Message.<String>builder()
                        .payload("ws_user" + userIdIncrement.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

        webSocketClient.closeFutureSync();
    }

}