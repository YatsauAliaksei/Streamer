package by.mrj.transport.websocket;

import by.mrj.domain.Command;
import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import by.mrj.transport.websocket.WebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.atomic.AtomicInteger;

import static by.mrj.transport.http.longpolling.LongPollingHttpClientTest.userIdIncrement;

@State(Scope.Benchmark)
@Slf4j
public class WebSocketClientTest {

    private static WebSocketClient webSocketClient = new WebSocketClient();

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
                        .payload("user" + userIdIncrement.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

        webSocketClient.closeFutureSync();
    }

}