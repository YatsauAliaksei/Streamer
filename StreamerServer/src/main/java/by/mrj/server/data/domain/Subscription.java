package by.mrj.server.data.domain;

import com.google.common.base.Preconditions;
import lombok.Value;
import org.springframework.util.StringUtils;

@Value
public class Subscription {

    private String clientId;
    private String topicName;

    public Subscription(String clientId, String topicName) {
        Preconditions.checkArgument(StringUtils.hasText(clientId), "Wrong clientId provided");
        Preconditions.checkArgument(StringUtils.hasText(topicName), "Unexpected topic name");

        this.clientId = clientId.toUpperCase();
        this.topicName = topicName.toUpperCase();
    }

    public String mapName() {
        return clientId + "_" + topicName;
    }
}
