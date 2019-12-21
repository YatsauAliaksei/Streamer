package by.mrj.server.service;

import by.mrj.server.data.HzConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DecisionService {

    public boolean shouldIProcess(String operation, String clientId) {
        // todo: global cluster member props. This service should decide which member should process

/*        if (!dataProvider.tryLock(clientId + HzConstants.Locks.USER_FETCH)) {
            log.debug("Already fetching for client [{}]", clientId);

            return;
        } else {
            log.info("Locking SubsToIds ...");
        }*/
        return true;
    }
}
