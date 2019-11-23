package by.mrj.server.service.sender;

import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.server.service.register.ClientRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Sends to {@param clientId} data returned from {@param dataToSend} supplier
     * @return - returns transformed by {@param transformer} topic name as a key and list {@link BaseObject#uuid}
     * which were sent to {@param clientId}
     */
    public Map<String, List<String>> sendTo(String clientId, Supplier<Collection<? extends BaseObject>> dataToSend, Function<String, String> transformer) {

        DataClient dataClient = clientRegister.findBy(clientId);

        if (dataClient == DataClient.DUMMY) {
            log.info("No registered client [{}] found. Nothing will be sent", clientId);

            return new HashMap<>();
        }

        ClientChannel streamingChannel = dataClient.getStreamingChannel();

        if (streamingChannel == null || !streamingChannel.getChannel().isActive()) {
            log.debug("No active channel found for [{}]. Nothing will be sent", clientId);

            return new HashMap<>();
        }

        Collection<? extends BaseObject> objects = dataToSend.get();
        if (objects == null || objects.isEmpty()) {
            log.debug("No data found to be sent");

            return new HashMap<>();
        }

        log.debug("Is about to send [{}] objects to [{}]", objects.size(), dataClient);

        // todo: easiest implementation All_At_Once strategy. Create diff types of sending strategies.
        streamingChannel.writeAndFlush(dataSerializer.serialize(objects));

        return objects.stream()
                .collect(Collectors.groupingBy(bo -> transformer.apply(bo.getTopic()),
                        Collector.of(
                                (Supplier<List<String>>) ArrayList::new,
                                (uuids, bo) -> uuids.add(bo.getUuid()),
                                (left, right) -> {
                                    left.addAll(right);
                                    return left;
                                },
                                Collector.Characteristics.IDENTITY_FINISH)));
    }

    public Map<String, List<String>> sendTo(String clientId, Supplier<Collection<? extends BaseObject>> dataToSend) {
        return sendTo(clientId, dataToSend, Function.identity());
    }
}
