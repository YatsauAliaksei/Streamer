package by.mrj.serialization;

import java.io.Serializable;

@FunctionalInterface
public interface DataSerializer {
    byte[] serialize(Serializable obj);
}
