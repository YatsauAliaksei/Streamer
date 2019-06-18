package by.mrj.domain.security;

import by.mrj.streamer.Topic;
import lombok.Getter;

import java.util.Set;

@Getter
public class Permission {

    private Set<Topic> topics;
}
