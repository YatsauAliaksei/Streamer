package by.mrj.common.domain.security;

import by.mrj.common.domain.streamer.Topic;
import lombok.Getter;

import java.util.Set;


@Getter
public class Permission {

    private Set<Topic> topics;
}
