package by.mrj.server.data.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DataToSend implements Serializable {

    private String clientId;
    private String topicName;
    @Builder.Default
    private Set<Long> ids = new HashSet<>();
}
