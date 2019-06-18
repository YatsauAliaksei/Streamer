package by.mrj.serialization;

import java.io.Serializable;

@FunctionalInterface
public interface DataSerializer {
    Object serialize(Serializable obj);
}
