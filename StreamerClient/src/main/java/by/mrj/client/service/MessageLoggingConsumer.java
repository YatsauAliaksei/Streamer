package by.mrj.client.service;

import by.mrj.common.domain.data.BaseObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class MessageLoggingConsumer implements MessageConsumer {

    private AtomicInteger total = new AtomicInteger();

    @Override
    public void consume(BaseObject[] msg) {

        if (msg == null || msg.length == 0) {
            log.warn("Received empty response");
            return;
        }

        List<String> uuids = Arrays.stream(msg)
                .map(BaseObject::getUuid)
                .collect(Collectors.toList());

        total.addAndGet(uuids.size());
        log.info("Messages received {}. Total: {}", uuids.size(), total.get());

    }

    @Override
    protected void finalize() throws Throwable {
        log.info("Total received: {}", total);

        super.finalize();
    }
}
