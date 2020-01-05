package by.mrj.common.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
@ToString
public class Statistic {
    private final double averageLatency;
    private final long totalReceivedMessages;
}
