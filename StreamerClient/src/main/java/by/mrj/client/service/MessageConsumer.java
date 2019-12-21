package by.mrj.client.service;

import by.mrj.common.domain.data.BaseObject;

public interface MessageConsumer {

    void consume(BaseObject[] msg);
}
