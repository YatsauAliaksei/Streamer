package by.mrj.data.kafka.adapter;

import by.mrj.data.DataProvider;
import by.mrj.domain.streamer.KafkaTopic;
import by.mrj.domain.streamer.Topic;
import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class KafkaDataProviderAdapter<T> {

    private final DataProvider<T> dataProvider;

    /**
     * Returns all available Data <T> based on {@param identifier} Permissions
     */
    public List<T> allAvailableData(String identifier) {
        List<Topic> availableTopics = getAvailableTopics(identifier);
        log.debug("Found topics [{}] for id [{}]", availableTopics, identifier);

        return availableTopics.stream()
                .flatMap(topic -> dataProvider.getAll(topic).stream())
                .collect(Collectors.toList());
    }

    /**
     * Returns available Topics based on {@param identifier} Permissions
     */
    public @NotNull List<Topic> getAvailableTopics(String identifier) {
        // todo: move to separate class.
        return Lists.newArrayList(KafkaTopic.builder().name("MAIN").build());
    }
}
