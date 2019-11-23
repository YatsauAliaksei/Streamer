package by.mrj.server.topic;

import by.mrj.common.domain.streamer.Topic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Builder
@ToString
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"subscriptions"})
//@RequiredArgsConstructor
public class GenericTopic implements Topic, Serializable {

    @Getter
    private final String name;
//    @Getter
//    @Builder.Default
//    private Map<String, Long> subscriptions = new ConcurrentHashMap<>();

/*    public void subscribe(String id) {
        subscriptions.put(id, 0L);
    }

    public void unsubscribe(String id) {
        subscriptions.remove(id);
    }

    @Override
    public void onTopicUpdate(ByteBuf byteBuf) {

        for (Map.Entry<String, Long> entry : subscriptions.entrySet()) {
            DataClient dataClient;
            String clientId = entry.getKey();
            if ((dataClient = clientRegister.findBy(clientId)) != null) {

                dataClient.getStreamingChannel().writeAndFlush(byteBuf);
            }
        }
    }

    @Override
    public void readFrom(long logId, int maxSize) {

    }*/
}
