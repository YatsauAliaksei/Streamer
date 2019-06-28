package by.mrj.transport.converter;

public interface MessageChannelConverter<IN, OUT> {
    OUT convert(IN dataToSend);
}
