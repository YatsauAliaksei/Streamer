package by.mrj.server.data.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

public class PostDataEvent extends ApplicationEvent {

    @Getter
    private final Map<String, List<Long>> topicToIds;

    public PostDataEvent(Object source, Map<String, List<Long>> topicToIds) {
        super(source);
        this.topicToIds = topicToIds;
    }
}
