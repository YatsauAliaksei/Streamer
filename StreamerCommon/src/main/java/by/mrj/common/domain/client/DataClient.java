package by.mrj.common.domain.client;

import by.mrj.common.domain.client.channel.ClientChannel;
import by.mrj.common.domain.streamer.Topic;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
@Builder
@ToString
@EqualsAndHashCode
public class DataClient implements Serializable {
    // FIXME:
    public static final DataClient DUMMY = DataClient.builder().id("DUMMY").build();

    private String id;
    @Builder.Default
    private ClientChannel streamingChannel;
}
