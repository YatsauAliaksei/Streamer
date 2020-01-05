package by.mrj.server.data.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = "timestamp")
public class DataUpdate {
    private final long timestamp = Instant.now().toEpochMilli();

    private final Long id;
    private final String client;
    private final String topic;
}
