package by.mrj.common.serialization.java;

import by.mrj.common.domain.Message;
import by.mrj.common.serialization.DataDeserializer;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.ObjectInputStream;

public class JavaDataDeserialization { // implements DataDeserializer {

//    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> Message<T> deserializeMessage(Object is, Class<T> clazz) {
        if (is instanceof InputStream) {
            ObjectInputStream ois = new ObjectInputStream((InputStream) is);
            return (Message<T>) ois.readObject();
        }
        throw new UnsupportedOperationException();
    }

//    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T deserialize(Object is, Class<T> clazz) {
        if (is instanceof InputStream) {
            ObjectInputStream ois = new ObjectInputStream((InputStream) is);
            return (T) ois.readObject();
        }
        throw new UnsupportedOperationException();
    }
}
