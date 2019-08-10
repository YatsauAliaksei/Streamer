package by.mrj.common.domain.client;

import by.mrj.domain.client.channel.ClientChannel;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class DataClient implements Serializable {
    // FIXME:
    public static final DataClient DUMMY = DataClient.builder().loginName("DUMMY").build();

    private String loginName;
    private ClientChannel streamingChannel;
}
