package by.mrj.transport.websocket.client;

import by.mrj.domain.Command;
import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Benchmark)
@Slf4j
public class WebSocketClientTest {

    private static WebSocketClient webSocketClient = new WebSocketClient();

//    @BeforeAll
    //    @Benchmark
//    @Fork(jvmArgsAppend = "-Xmx2g")
//    @BenchmarkMode(Mode.SingleShotTime)
//    @Threads(2)
    @Setup(Level.Invocation)
    public static void before() throws InterruptedException {
/*        webSocketClient.registerHandshakeListener(future -> webSocketClient.send(
                Message.<String>builder()
                        .payload("myLoginName")
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build()));*/

//        log.info("Creating channel...");
        webSocketClient.createChannel();

//        log.info("Channel created");

        Thread.sleep(500L);
    }

    private AtomicInteger iterator = new AtomicInteger();

//    @Test
    @Benchmark
    public void send() throws InterruptedException {
        log.debug("Sending benchmark message...");

        webSocketClient.send(
                Message.<String>builder()
                        .payload("myLoginName-" + iterator.incrementAndGet())
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.CONNECT)
                        .build());

//        while (webSocketClient.channel.isActive()) {
//            Thread.sleep(1_000L);
//            System.out.println("Still active...");
//        }
//
//        System.out.println("Channel not active");

        webSocketClient.closeFutureSync();
    }

}