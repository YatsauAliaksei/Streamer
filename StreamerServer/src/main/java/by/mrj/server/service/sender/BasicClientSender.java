package by.mrj.server.service.sender;

import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.utils.DataUtils;
import by.mrj.server.service.register.ClientRegister;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limit.VegasLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Log4j2
@Component
@RequiredArgsConstructor
public class BasicClientSender {

    private final ClientRegister clientRegister;
    private final DataSerializer dataSerializer;

    private VegasLimit limit = VegasLimit.newBuilder()
            .initialLimit(3_000)
            .maxConcurrency(10_000)
            .build();

    private SendLimiter limiter = SendLimiter.newBuilder().limit(limit).build();

    /**
     * Sends to {@param clientId} data returned from {@param dataToSend} supplier
     * @return - returns transformed by {@param transformer} topic name as a key and list {@link BaseObject#id}
     * which were sent to {@param clientId}
     */
    public Map<String, List<Long>> sendTo(String clientId, Function<Integer, Collection<BaseObject>> dataToSend, Function<String, String> transformer) {
        Optional<Limiter.Listener> acquire = limiter.acquire(null);

        if (!acquire.isPresent()) {
            log.warn("Under high load. Rejecting send operation for [{}]", clientId);

            return new HashMap<>();
        }

        int limit = limiter.getLimit();
        Collection<BaseObject> objects = dataToSend.apply(limit); // todo: fixme

        if (objects == null || objects.isEmpty()) {
            log.debug("No data found to be sent");

            return new HashMap<>();
        }

        DataClient dataClient = clientRegister.findBy(clientId);
        ClientChannel streamingChannel = dataClient.getStreamingChannel();

        log.debug("Sending {} objects with limit {} to {}", objects.size(), limit, clientId);

        streamingChannel.writeAndFlush(dataSerializer.serialize(objects));

        Map<String, List<Long>> sentObjs = DataUtils.topicToIds(objects, transformer);

        Limiter.Listener listener = acquire.get();
        listener.onSuccess();

        return sentObjs;
    }

    public void send(String clientId, Object objToSend) {

        DataClient dataClient = clientRegister.findBy(clientId);
        if (dataClient == DataClient.DUMMY) {
            log.info("No client [{}] found", clientId);
            return;
        }

        ClientChannel streamingChannel = dataClient.getStreamingChannel();
        if (!streamingChannel.getChannel().isActive()) {
            log.info("No active channel for [{}] found", clientId);
            return;
        }

        log.debug("Is about to send [{}] objects to [{}]", objToSend, dataClient);

        streamingChannel.writeAndFlush(dataSerializer.serialize(objToSend));
    }

    public Map<String, List<Long>> sendTo(String clientId, Function<Integer, Collection<BaseObject>> dataToSend) {
        return sendTo(clientId, dataToSend, Function.identity());
    }
}
