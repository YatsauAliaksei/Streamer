package by.mrj.domain.client;

import by.mrj.domain.StreamingChannel;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
//@NoArgsConstructor
public class DataClient implements Serializable {
    private String loginName;
    private ConnectionInfo connectionInfo;
    private StreamingChannel streamingChannel;
}
