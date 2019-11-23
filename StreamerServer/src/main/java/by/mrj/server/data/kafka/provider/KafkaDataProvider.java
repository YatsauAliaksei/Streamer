package by.mrj.server.data.kafka.provider;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.domain.streamer.Topic;
import by.mrj.server.data.DataProvider;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

// TODO: Seems Kafka doesn't feet to our needs as we need to have possibility to modify message
//@Component
@RequiredArgsConstructor
public class KafkaDataProvider {

    private final KafkaProducer<String, String> producer;
    private final KafkaConsumer<String, String> consumer;

/*    @Override
    public List<BaseObject> getAllForUser(Topic topic) {
        consumer.seek(new TopicPartition("", 1), 10);
    }

    @Override
    public void putAll(Topic topic, List<BaseObject> baseObjects) {
        for (BaseObject baseObject : baseObjects) {
            producer.send(new ProducerRecord<>(topic.getName(), baseObject.getUuid(), baseObject.getPayload()));
        }
    }*/
}
