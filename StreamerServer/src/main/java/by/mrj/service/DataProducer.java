package by.mrj.service;

import by.mrj.domain.BaseObject;
import by.mrj.domain.Message;
import by.mrj.domain.client.DataClient;
import by.mrj.serialization.DataSerializer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor(staticName = "create", access = AccessLevel.PUBLIC)
public class DataProducer {

    public static int SIZE = 50; // TODO: remove

    private final DataSerializer serializer;

    public List<byte[]> findData(DataClient client) {

//        return testData();
        return Collections.emptyList();
    }

    private static Message<BaseObject> createMessage(int _1st) {
        return Message.<BaseObject>builder()
                .payload(BaseObject.builder()
//                        ._1st(_1st)
//                        .text("Hello")
                        .build())
                .build();
    }
}
