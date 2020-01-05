package by.mrj.server.service.sender.strategy;

import by.mrj.server.data.DataProvider;
import by.mrj.server.data.domain.DataToSend;
import by.mrj.server.job.RingBufferRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBasedRegister {

    private final DataProvider dataProvider;
    private final RingBufferRegister ringBufferRegister;

    public void eventBased(String clientId) {
        Map<String, Collection<Long>> uuids = dataProvider.getAllUuids(clientId);

        for (Map.Entry<String, Collection<Long>> entry : uuids.entrySet()) {
            var data = new DataToSend(clientId, entry.getKey(), new HashSet<>(entry.getValue()));
            ringBufferRegister.register(data);
        }
    }
}
