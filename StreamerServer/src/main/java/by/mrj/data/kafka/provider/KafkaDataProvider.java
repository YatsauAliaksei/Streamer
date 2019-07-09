package by.mrj.data.kafka.provider;

import by.mrj.data.DataProvider;
import by.mrj.domain.Command;
import by.mrj.domain.Message;
import by.mrj.domain.MessageHeader;
import by.mrj.domain.streamer.Topic;
import by.mrj.serialization.DataSerializer;
import by.mrj.serialization.json.JsonJackson;
import by.mrj.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO: Seems Kafka doesn't feet to our needs as we need to have possibility to modify message
@RequiredArgsConstructor
public class KafkaDataProvider implements DataProvider<ByteBuf> {

    // todo: tmp solution
    private final DataSerializer serializer;
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
                                .command(Command.READ)
                                .build(),
                        Message.<String>builder().payload("Msg-" + i).build())
                ).collect(Collectors.toList());
    }
}
