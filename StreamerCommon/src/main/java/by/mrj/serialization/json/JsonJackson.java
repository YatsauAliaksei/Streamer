package by.mrj.serialization.json;

import by.mrj.domain.Message;
import by.mrj.serialization.DataDeserializer;
import by.mrj.serialization.DataSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;

@Slf4j
// TODO: think about using Spring JsonJackson approach
public class JsonJackson implements DataDeserializer, DataSerializer {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // to enable standard indentation ("pretty-printing"):
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // to allow serialization of "empty" POJOs (no properties to serialize)
        // (without this setting, an exception is thrown in those cases)
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // to write java.util.Date, Calendar as number (timestamp):
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // DeserializationFeature for changing how JSON is read as POJOs:
        // to prevent exception when encountering unknown property:
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Exception during serialization.", e);
            log.error("Serialized object [{}]", obj);
            return "";
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Exception during deserialization", e);
            log.error("Data [{}]. To class [{}]", json, clazz);
            throw new RuntimeException(e);
        }
    }

    public static <T> Message<T> msgFromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, new TypeReference<Message<T>>(){});
        } catch (IOException e) {
            log.error("Exception during deserialization", e);
            log.error("Data [{}]. To class [{}]", json, clazz);
            throw new RuntimeException(e);
        }
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> Message<T> deserializeMessage(Object json, Class<T> clazz) {
        if (json instanceof String) {
            return msgFromJson((String) json, clazz);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T deserialize(Object json, Class<T> clazz) {
        if (json instanceof String) {
            return fromJson((String) json, clazz);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String serialize(Serializable obj) {
        return toJson(obj);
    }
}
