package by.mrj.common.serialization;


import by.mrj.common.domain.Message;

public interface DataDeserializer {

    <T> Message<T> deserializeMessage(Object obj, Class<T> clazz);

    <T> T deserialize(Object obj, Class<T> clazz);
}
