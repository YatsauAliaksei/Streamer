package by.mrj.common.serialization.java;

import by.mrj.common.serialization.DataSerializer;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class JavaDataSerializer { //  implements DataSerializer {

//    @Override
    @SneakyThrows
    public byte[] serialize(Serializable obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        }
    }
}
