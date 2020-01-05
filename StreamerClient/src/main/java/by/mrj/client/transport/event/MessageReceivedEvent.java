package by.mrj.client.transport.event;

import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.data.BaseObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class MessageReceivedEvent extends ApplicationEvent {

    @Getter
    private final BaseObject[] data;
    @Getter
    private final ConnectionInfo connectionInfo;

    /**
     * Create a new ApplicationEvent.
     * @param source the object on which the event initially occurred (never {@code null})
     * @param msg
     */
    public MessageReceivedEvent(Object source, BaseObject[] msg, ConnectionInfo connectionInfo) {
        super(source);

        this.data = msg;
        this.connectionInfo = connectionInfo;
    }
}
