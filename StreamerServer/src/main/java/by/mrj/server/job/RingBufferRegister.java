package by.mrj.server.job;

import by.mrj.common.domain.client.DataClient;
import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import by.mrj.server.data.domain.DataToSend;
import by.mrj.server.data.domain.SendStatus;
import by.mrj.server.service.MultiMapService;
import by.mrj.server.service.sender.LockingSender;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.ringbuffer.OverflowPolicy;
import com.hazelcast.ringbuffer.Ringbuffer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class RingBufferRegister implements InitializingBean /*, NewClientRegistrationListener */{

    private static final String RB_JOBS = "rb_jobs";

    private final AtomicLong seq = new AtomicLong();

    private final DataProvider dataProvider;
    private final LockingSender lockingSender;
    private final MultiMapService multiMapService;
    private final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();

    @Setter
    private Executor executorsPool = Executors.newFixedThreadPool(8, new ThreadFactoryBuilder().setNameFormat("reg-%d").build());
    @Setter
    private ScheduledExecutorService scheduledExecutorPool = Executors.newScheduledThreadPool(8, new ThreadFactoryBuilder().setNameFormat("reg-scheduled-%d").build());

    public void register(DataToSend data) {
        log.debug("Registering send job for [{}] size [{}]", data.getClientId(), data.getIds().size());

        Ringbuffer<DataToSend> rb = dataProvider.getRingBuffer(RB_JOBS + "_TMP", DataToSend.class);
        rb.addAsync(data, OverflowPolicy.OVERWRITE)
                .andThen(new ExecutionCallback<Long>() {

                             @Override
                             public void onResponse(Long response) {
                                 log.debug("Registered RB-{}", response);
                             }

                             @Override
                             public void onFailure(Throwable t) {
                                 log.error("Failed.", t);

                                 multiMapService.saveToMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS,
                                         HazelcastDataProvider.createSubsToIdsKey(data.getClientId(), data.getTopicName()), data.getIds());
                             }
                         }
                );

        multiMapService.removeFromMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS,
                HazelcastDataProvider.createSubsToIdsKey(data.getClientId(), data.getTopicName()),
                data.getIds());
    }

    private void run() {

        Ringbuffer<DataToSend> rb = dataProvider.getRingBuffer(RB_JOBS + "_TMP", DataToSend.class);

        new Thread(() -> {

            while (dataProvider.wasSent(seq.get())) { // move to 1st not sent position
                seq.incrementAndGet();
            }

            log.info("Starting sending from seq [{}]", seq.get());

            while (true) { // todo: catch everything...

                DataToSend data;
                long index = seq.getAndIncrement(); // todo: should be align with RB capacity to reset index
                try {
                    // todo: lock start
                    log.debug("Reading RB-{}", index);

                    if (dataProvider.wasSent(index)) {
                        continue;
                    }

                    data = rb.readOne(index); // blocking
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                log.debug("Sending data. [{}] - [{}]", data.getClientId(), data.getIds().size());

                CompletableFuture.runAsync(() -> send(seq, data, index), executorsPool);
            }

        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    Long index = queue.take(); // blocking
                    if (!dataProvider.wasSent(index)) {
                        log.debug("Trying to resend {}", index);

                        DataToSend data = rb.readOne(index);

                        scheduledExecutorPool.schedule(() -> send(seq, data, index), 5000, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

//    @Override
    public void handleNewRegistration(DataClient dataClient) {
        log.info("RB new client registration handled. [{}]", dataClient);
    }

    private void send(AtomicLong seq, DataToSend data, long index) {
        SendStatus sendStatus = lockingSender.sendAndRemove(data);

        if (sendStatus == SendStatus.IN_PROGRESS) {
            log.debug("Setting index to [{}] for size [{}]", index, data.getIds().size());

            seq.set(index);
        }

        if (sendStatus == SendStatus.OK) {
            dataProvider.markAsSent(index);
        }

        if (sendStatus == SendStatus.NO_ACTIVE_CHANNEL) {
            log.debug("Adding to queue {} size {}", index, data.getIds().size());
            queue.add(index);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        run();
    }
}
