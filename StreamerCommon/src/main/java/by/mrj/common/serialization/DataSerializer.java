package by.mrj.common.serialization;

@FunctionalInterface
public interface DataSerializer {
    String serialize(Object obj);
}
