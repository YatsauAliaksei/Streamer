package by.mrj.serialization.json;

import by.mrj.common.domain.Message;
import by.mrj.common.serialization.json.JsonJackson;
import org.junit.jupiter.api.Test;

class JsonJacksonTest {
    JsonJackson jackson = new JsonJackson();

    @Test
    void deserialize() {
        String hi = jackson.serialize(Message.<String>builder().payload("Hi").build());

        Message<String> msg = jackson.deserializeMessage(hi, String.class);
        System.out.println(msg);

    }

    @Test
    void serialize() {
    }
}