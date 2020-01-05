package by.mrj.client.service;

import by.mrj.common.domain.Statistic;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.data.BaseObject;

public interface MessageConsumer {

    void consume(BaseObject[] msg, ConnectionInfo connectionInfo);

    Statistic statistics();
}
