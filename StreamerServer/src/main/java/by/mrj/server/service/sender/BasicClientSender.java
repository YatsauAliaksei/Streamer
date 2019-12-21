package by.mrj.server.service.sender;

import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.server.service.register.ClientRegister;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limit.VegasLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class BasicClientSender {

    private final ClientRegister clientRegister;
    private final DataSerializer dataSerializer;
//    private final SendJobRegister jobRegister;
//    private final DataProvider dataProvider;

    private VegasLimit limit = VegasLimit.newBuilder()
            .initialLimit(1000)
            .maxConcurrency(4000)
            .build();

    private SendLimiter limiter = SendLimiter.newBuilder().limit(limit).build();

    /**
     * Sends to {@param clientId} data returned from {@param dataToSend} supplier
     * @return - returns transformed by {@param transformer} topic name as a key and list {@link BaseObject#uuid}
     * which were sent to {@param clientId}
     */
    public Map<String, List<String>> sendTo(String clientId, Function<Integer, Collection<? extends BaseObject>> dataToSend, Function<String, String> transformer) {
        Optional<Limiter.Listener> acquire = limiter.acquire(null);

        if (!acquire.isPresent()) {
            log.warn("Under high load. Rejecting send operation for [{}]", clientId);

            return new HashMap<>();
        }

        int limit = limiter.getLimit();
        Collection<? extends BaseObject> objects = dataToSend.apply(limit);

        if (objects == null || objects.isEmpty()) {
            log.info("No data found to be sent");

            return new HashMap<>();
        }

        DataClient dataClient = clientRegister.findBy(clientId);
        ClientChannel streamingChannel = dataClient.getStreamingChannel();

        log.debug("Is about to send [{}] objects to [{}]", objects.size(), dataClient);

        log.info("Sending {} objects with limit {}", objects.size(), limit);
        streamingChannel.writeAndFlush(dataSerializer.serialize(objects));

        Map<String, List<String>> sentObjs = objects.stream()
                .collect(Collectors.groupingBy(bo -> transformer.apply(bo.getTopic()),
                        Collector.of(
                                (Supplier<List<String>>) ArrayList::new,
                                (uuids, bo) -> uuids.add(bo.getUuid()),
                                (left, right) -> {
                                    left.addAll(right);
                                    return left;
                                },
                                Collector.Characteristics.IDENTITY_FINISH)));

        Limiter.Listener listener = acquire.get();
        listener.onSuccess();

        return sentObjs;
    }

    public Map<String, List<String>> sendTo(String clientId, Function<Integer, Collection<? extends BaseObject>> dataToSend) {
        return sendTo(clientId, dataToSend, Function.identity());
    }
}
