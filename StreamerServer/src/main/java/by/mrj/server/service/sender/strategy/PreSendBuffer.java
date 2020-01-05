package by.mrj.server.service.sender.strategy;

import by.mrj.server.data.domain.DataToSend;
import by.mrj.server.data.domain.DataUpdate;
import by.mrj.server.job.RingBufferRegister;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
// TODO: Shows wrong results on > 10_000 updates. Some issue with concurrency
// Anyway shows overall worse results then EventBased especially on small update batches
public class PreSendBuffer {

    private final RingBufferRegister ringBufferRegister;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), // todo: configurable
            new ThreadFactoryBuilder().setNameFormat("buffer-%d").build());

    private final ConcurrentMap<String, Deque<DataUpdate>> buffer = new ConcurrentHashMap<>(50_000); // todo: configurable
    private final ConcurrentMap<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>(10_000); // todo: configurable
    private long TIME_LIMIT = TimeUnit.MILLISECONDS.toMillis(20);

    public void add(DataUpdate dataUpdate) {
        String clientId = dataUpdate.getClient();

        var q = buffer.get(clientId);

        if (q == null) {
            q = new ConcurrentLinkedDeque<>();
            buffer.put(clientId, q); // todo: remove when queue empty
        }

        DataUpdate prev = q.peekLast();
        q.add(dataUpdate);

        checkLimit(prev, dataUpdate, q);
    }

    private void checkLimit(DataUpdate prev, DataUpdate curr, Queue<DataUpdate> queue) {

        if (!cancelJobIfExist(curr.getClient()))
            return;

        if (shouldSend(prev, curr, queue)) {
            register(curr, queue);
        } else {
            // todo: maybe this is a problem. When 100+ requests come here
            ScheduledFuture<?> schedule = executor.schedule(() -> register(curr, queue), 50, TimeUnit.MILLISECONDS);
            scheduledJobs.put(curr.getClient(), schedule);

            log.debug("Registered 'REGISTER' job");
        }
    }

    private boolean cancelJobIfExist(String clientId) {
        ScheduledFuture<?> scheduledFuture = scheduledJobs.get(clientId);
        if (scheduledFuture == null) {
            log.debug("No job found...");

            return true;
        }

        if (scheduledFuture.isDone()) {
            log.debug("Job is done already.");

            return true;
        }

        if (!scheduledFuture.cancel(false)) {
            log.debug("Cannot cancel running job. ");

            return false;
        }

        log.debug("Job canceled for {}.", clientId);
        return true;
    }

    private void register(DataUpdate curr, Queue<DataUpdate> queue) {
        Set<DataUpdate> datas = new HashSet<>(queue);
        queue.removeAll(datas);

        CompletableFuture.runAsync(() -> {

            Map<String, Set<Long>> topicToUuids = datas.stream()
                    .collect(Collectors.groupingBy(DataUpdate::getTopic,
                            Collector.of(HashSet::new,
                                    (uuids, bo) -> uuids.add(bo.getId()),
                                    (left, right) -> {
                                        left.addAll(right);
                                        return left;
                                    },
                                    Collector.Characteristics.IDENTITY_FINISH)));

            for (Map.Entry<String, Set<Long>> entry : topicToUuids.entrySet()) {
                DataToSend dataToSend = new DataToSend(curr.getClient(), entry.getKey(), entry.getValue());

                ringBufferRegister.register(dataToSend);
            }
        });
    }

    private boolean shouldSend(DataUpdate prev, DataUpdate curr, Queue<DataUpdate> queue) {
        if (prev == null) {
            log.info("No prev found.");
            return false;
        }

        boolean predicate1 = (curr.getTimestamp() - prev.getTimestamp()) > TIME_LIMIT;
        if (predicate1) {
            log.info("Should send Predicate TIME DIFF violation");
            return true;
        }

        boolean predicate2 = queue.size() >= 1000;
        if (predicate2) {
            log.info("Should send Predicate SIZE LIMIT violation");
            return true;
        }

        return false;
    }
}
