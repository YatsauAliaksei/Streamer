package by.mrj;

import by.mrj.controller.CommandListener;
import by.mrj.transport.PortListener;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// TODO: Should be Singleton or one time invoker
@Slf4j
@Builder
@RequiredArgsConstructor
public class Bootstrap {

    private final PortListener portListener;
    private final CommandListener commandListener;

    public void run() {
        log.info("Running port listener [{}]", portListener);
        portListener.listen(commandListener);
    }
}
