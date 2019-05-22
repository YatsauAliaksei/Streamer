package by.mrj.serialization.java;

import by.mrj.domain.Message;
import by.mrj.serialization.DataDeserializer;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class JavaDataDeserialization implements DataDeserializer {

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T extends Serializable> Message<T> deserialize(InputStream is, Class<T> clazz) {
        ObjectInputStream ois = new ObjectInputStream(is);
        return (Message<T>) ois.readObject();
    }
}
