package by.mrj.performance.socket;

import by.mrj.domain.BaseObject;
import by.mrj.domain.Message;
import by.mrj.serialization.DataSerializer;
import by.mrj.serialization.java.JavaDataSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class DataStreamer {

    public static int SIZE = 50;

    private DataSerializer serializer = new JavaDataSerializer();
    private ExecutorService serverPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r ->
            new Thread(r, "Server pool")
    );

    public static void main(String[] args) throws Exception {
        DataStreamer dataStreamer = new DataStreamer();
        dataStreamer.runListener();
    }

    public void runListener() {
//        var producer = createProducer();
        List<byte[]> data = IntStream.range(0, SIZE)
                .boxed()
                .map(DataStreamer::createMessage)
//                .peek(o -> {
//                    System.out.println("Size: " + ObjectSize.getObjectSize(o.getPayload()));
//                })
                .map(serializer::serialize)
                .collect(Collectors.toList());

        log.info("Message size: {} bytes", data.get(0).length);

        BasicNetServerSocket serverSocket = new BasicNetServerSocket(8181);

        log.info("Listening...");

        while (true) {
            NetSocket socket = serverSocket.accept();
            serverPool.submit(() -> {

//                log.debug("Got client request: ");

                for (byte[] message : data) {
                    try {

//                        log.debug("writing...");
                        socket.outputStream().write(message);
                        socket.outputStream().flush();
//                        log.debug("Response sent");
//                    }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private static Message<BaseObject> createMessage(int _1st) {
        return Message.<BaseObject>builder()
                .payload(BaseObject.builder()
//                        ._1st(_1st)
//                        .text("Hello")
                        .build())
                .build();
    }
}
