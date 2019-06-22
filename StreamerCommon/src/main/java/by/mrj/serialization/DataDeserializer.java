package by.mrj.serialization;

import by.mrj.domain.Message;

public interface DataDeserializer {

    <T> Message<T> deserializeMessage(Object obj, Class<T> clazz);

    <T> T deserialize(Object obj, Class<T> clazz);
}
