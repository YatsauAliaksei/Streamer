package by.mrj.performance;

import by.mrj.domain.BaseObject;
import by.mrj.domain.Message;
import by.mrj.performance.socket.Client;
import by.mrj.performance.socket.DataStreamer;
import by.mrj.serialization.DataDeserializer;
import by.mrj.serialization.java.JavaDataDeserialization;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@State(Scope.Benchmark)
public class PerformanceCheck {

    private DataDeserializer deserializer = new JavaDataDeserialization();

    public static void main(String[] args) {
        PerformanceCheck performanceCheck = new PerformanceCheck();
        ExecutorService clientPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r ->
                new Thread(r, "Client pool")
        );

        int concurrency = 100;
//        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()));
        while (concurrency-- > 0) {
            clientPool.submit(performanceCheck::runCheck);
        }
//        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()));
    }

    List<Client> clients = IntStream.range(0, 1_000)
            .boxed()
            .map(k -> new Client(null, 8181))
            .collect(Collectors.toList());

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
//    @SneakyThrows
    public void runCheck() {
//        log.debug("Client connecting...");

//        Thread.sleep(100);

        AtomicInteger counter = new AtomicInteger();
        clients.parallelStream().forEach(client -> {
            InputStream is = client.getInputStream();
            int k = DataStreamer.SIZE;
            while (k > 0) {
                try {
                    if (is.available() == 0) {
//                        log.debug("No data...");
                        Thread.sleep(1);
                    } else {
//                        log.debug("Receiving data...");
                        k--;

                        Message<BaseObject> message = deserializer.deserialize(is, BaseObject.class);
                        counter.incrementAndGet();
//                        log.debug("Message [{}]", message);
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        log.info("Total: {}", counter.get());
    }
}
