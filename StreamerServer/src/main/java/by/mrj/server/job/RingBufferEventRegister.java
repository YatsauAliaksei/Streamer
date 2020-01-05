package by.mrj.server.job;

import by.mrj.server.data.DataProvider;
import by.mrj.server.data.domain.SendStatus;
import by.mrj.server.service.sender.LockingSender;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.ringbuffer.OverflowPolicy;
import com.hazelcast.ringbuffer.Ringbuffer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RingBufferEventRegister implements InitializingBean {

    private static final String RB_JOBS = "rb_jobs";

    private final DataProvider dataProvider;
    private final LockingSender lockingSender;

    @Setter
    private Executor executorsPool = Executors.newFixedThreadPool(8, new ThreadFactoryBuilder().setPriority(8).setNameFormat("reg-%d").build());

    public void register(String clientId) {
        log.debug("Registering send job for [{}] event", clientId);

        Ringbuffer<String> rb = dataProvider.getRingBuffer(RB_JOBS, String.class);
        rb.addAsync(clientId, OverflowPolicy.OVERWRITE);
    }

    public void run() {

        Ringbuffer<String> rb = dataProvider.getRingBuffer(RB_JOBS, String.class);
        IAtomicLong rbSeq = dataProvider.getSequence("RingBuffer_SEQ");

        long initHead = rb.headSequence();
        rbSeq.set(initHead);

        new Thread(() -> {

            log.info("Starting sending from seq [{}]", initHead);

            while (true) { // todo: catch everything...

                String clientId;
                try {
                    clientId = rb.readOne(rbSeq.getAndIncrement()); // blocking
                } catch (InterruptedException e) {
                    throw new RuntimeException(e); // todo: definitely wrong
                }

                log.debug("Sending data to [{}]", clientId);

                CompletableFuture.runAsync(() -> send(clientId), executorsPool)
                        .exceptionally(t -> {
                            log.error("Error while sending for [" + clientId + "]", t);
                            return null;
                        });
            }

        }).start();
    }

    private void send(String clientId) {
        SendStatus sendStatus = lockingSender.sendAndRemove(clientId);

        if (sendStatus == SendStatus.CONTINUE) {
            log.debug("Continue command received. Registering event for WS. [{}]", clientId);

            register(clientId);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        run();
    }
}
