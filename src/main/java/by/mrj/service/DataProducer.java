package by.mrj.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor(staticName = "create", access = AccessLevel.PUBLIC)
public class DataProducer<T> implements Supplier<T> {

    private final Supplier<T> supplier;

    @Override
    public T get() {
        return supplier.get();
    }
}
