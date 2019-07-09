package by.mrj.domain.client;

import by.mrj.domain.StreamingChannel;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class DataClient implements Serializable {
    // FIXME:
    public static final DataClient DUMMY = DataClient.builder().loginName("DUMMY").build();

    private String loginName;
    private ConnectionInfo connectionInfo;
    private StreamingChannel streamingChannel;
}
