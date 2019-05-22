package by.mrj.serialization;

import by.mrj.domain.Message;

import java.io.InputStream;
import java.io.Serializable;

@FunctionalInterface
public interface DataDeserializer {
    <T extends Serializable> Message<T> deserialize(InputStream is, Class<T> clazz);
}
