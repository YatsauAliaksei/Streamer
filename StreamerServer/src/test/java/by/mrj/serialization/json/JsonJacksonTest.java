package by.mrj.serialization.json;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.IntStream;

@Slf4j
class JsonJacksonTest {
    JsonJackson jackson = new JsonJackson();

    @Test
    void deserialize() {
        for (int i = 0; i < 2; i++) {

            BaseObject bo = BaseObject.builder()
                    .payload(String.valueOf(Instant.now().toEpochMilli()))
                    .topic("First")
                    .build();
            var list = Lists.newArrayList(bo);

            log.info("About to serialize: {}", list);

            String boJson = JsonJackson.toJson(list);

            log.info("JSON: {}", boJson);

            BaseObject[] msg = jackson.deserialize(boJson, BaseObject[].class);
            log.info("Object: {}", Arrays.toString(msg));

        }

    }

    @Test
    void serialize() {
    }
}