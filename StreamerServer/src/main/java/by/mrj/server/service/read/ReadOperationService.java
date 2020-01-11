package by.mrj.server.service.read;

import by.mrj.server.job.RingBufferEventRegister;
import by.mrj.server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadOperationService {

    private final RingBufferEventRegister ringBufferEventRegister;

    public void readAll() {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow((() -> new IllegalStateException("Unauthorized user"))); // fixme: should not ever happen here. Do we really need Optional here

        ringBufferEventRegister.register(currentUserLogin);
    }
}
