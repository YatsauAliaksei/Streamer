package by.mrj.server.data.kafka.provider;

import by.mrj.common.domain.Command;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.streamer.Topic;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.serialization.json.JsonJackson;
import by.mrj.common.utils.ByteBufUtils;
import by.mrj.server.data.DataProvider;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO: Seems Kafka doesn't feet to our needs as we need to have possibility to modify message
@Component
@RequiredArgsConstructor
public class KafkaDataProvider implements DataProvider<ByteBuf> {

    // todo: tmp solution
    private final DataSerializer dataSerializer;
    // todo: remove
    private List<ByteBuf> testData = createTestData(10);

    @Override
    public List<ByteBuf> getAll(Topic topic) {
        return testData;
    }

    // todo: remove
    private List<ByteBuf> createTestData(int size) {
        DataSerializer serializer = new JsonJackson();
        return IntStream.range(0, size).boxed()
                .map(i -> ByteBufUtils.create(serializer,
                        MessageHeader.builder()
                                .command(Command.POST)
                                .build(),
                        Message.<String>builder().payload("Msg-" + i).build())
                ).collect(Collectors.toList());
    }
}
