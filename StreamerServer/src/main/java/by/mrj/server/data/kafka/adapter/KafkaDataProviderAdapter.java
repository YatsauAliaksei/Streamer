package by.mrj.server.data.kafka.adapter;

import by.mrj.common.domain.streamer.KafkaTopic;
import by.mrj.common.domain.streamer.Topic;
import by.mrj.server.data.DataProvider;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
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
    public List<Topic> getAvailableTopics(String identifier) {
        // todo: move to separate class.
        return Lists.newArrayList(KafkaTopic.builder().name("MAIN").build());
    }
}
