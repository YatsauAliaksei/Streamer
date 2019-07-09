package by.mrj.domain.streamer;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
public class KafkaTopic implements Topic {

    @Getter
    private final String name;
}
