package by.mrj.serialization.json;

import by.mrj.common.domain.Message;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;
import by.mrj.common.utils.DataUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class JsonJacksonTest {
    JsonJackson jackson = new JsonJackson();

    @Test
    void deserialize() {
//        String hi = jackson.serialize(Message.<String>builder().payload("Hi").build());
        String hi = jackson.serialize(DataUtils.createNewData("First", null, Instant.now().toEpochMilli()));
        System.out.println("Serialized: " + hi);

        BaseObject msg = jackson.deserialize(hi, BaseObject.class);
        System.out.println(msg);

    }

    @Test
    void deserializeList() {
/*        List<GenericTopic> payload = Lists.newArrayList(
                GenericTopic.builder().name("topic_name1").build(),
                GenericTopic.builder().name("topic_name2").build());

        String incomeMsg = jackson.serialize(Message.<List<GenericTopic>>builder()
                .payload(payload)
                .build());

        Message<GenericTopic[]> msg = jackson.deserializeMessage(incomeMsg, GenericTopic[].class);*/

//        System.out.println(msg);
    }

    @Test
    void serialize() {
    }
}