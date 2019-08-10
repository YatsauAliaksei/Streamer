package by.mrj.common.serialization;

import java.io.Serializable;

@FunctionalInterface
public interface DataSerializer {
    Object serialize(Serializable obj);
}
