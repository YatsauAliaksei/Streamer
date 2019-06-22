package by.mrj.serialization.json;

import by.mrj.domain.Message;
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