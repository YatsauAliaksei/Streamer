package by.mrj.client.service;

import by.mrj.client.transport.event.MessageReceivedEvent;
import by.mrj.common.domain.Statistic;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.data.BaseObject;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RequiredArgsConstructor
public class MessageLoggingConsumer implements MessageConsumer {

    private final ApplicationEventPublisher publisher;

    private AtomicInteger total = new AtomicInteger();
    private ConcurrentMap<String, AtomicInteger> counter = new ConcurrentHashMap<>();
    private ConcurrentMap<String, AtomicDouble> latency = new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();

    @Override
    public void consume(BaseObject[] msg, ConnectionInfo connectionInfo) {
        long now = Instant.now().toEpochMilli();

        publisher.publishEvent(new MessageReceivedEvent(this, msg, connectionInfo));

        int batchSize;
        if (msg == null || (batchSize = msg.length) == 0) {
            log.warn("Received empty response");
            return;
        }

        for (BaseObject bo : msg) {
            Long id = bo.getId();

            if (id == null) {
                log.debug("Service messages. [{}]", bo); // typically comes alone
                return;
            }
        }

        String login = connectionInfo.getLogin();

        lock.lock();

        AtomicInteger msgsReceived = counter.computeIfAbsent(login, k -> new AtomicInteger());
        AtomicDouble avgLatency = latency.computeIfAbsent(login, k -> new AtomicDouble());

        double receivedLatency = Arrays.stream(msg)
                .map(BaseObject::getPayload)
                .mapToLong(p -> now - Long.valueOf(p))
                .average().orElse(0);

        int totalMessages = msgsReceived.get() + batchSize;

        double avg = ((avgLatency.get() * msgsReceived.get()) + (receivedLatency * batchSize)) / totalMessages;
        avgLatency.set(avg);

        lock.unlock();

        int sizeForClient = msgsReceived.addAndGet(batchSize);
        int t = total.addAndGet(batchSize);

        log.info("Messages received {}/{}, ReceivedLatency: {}, AvgLatency: {} for {}. Total: {}", batchSize, sizeForClient, receivedLatency, avg, login, t);
    }

    @Override
    public Statistic statistics() {

        double totalAvg = latency.values().stream()
                .mapToDouble(AtomicDouble::doubleValue)
                .average().orElse(0);

        return Statistic.builder()
                .averageLatency(totalAvg)
                .totalReceivedMessages(total.get())
                .build();
    }

    @Override
    protected void finalize() throws Throwable {
        log.info("Total received: {}", total);

        super.finalize();
    }
}
