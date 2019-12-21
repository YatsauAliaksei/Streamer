package by.mrj.server.topic;

import by.mrj.common.domain.streamer.Topic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Builder
@ToString
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"subscriptions"})
public class GenericTopic implements Topic, Serializable {

    @Getter
    private final String name;
}
