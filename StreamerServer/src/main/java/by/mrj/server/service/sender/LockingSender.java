package by.mrj.server.service.sender;

import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import by.mrj.server.data.domain.DataToSend;
import by.mrj.server.data.domain.SendStatus;
import by.mrj.server.data.domain.Subscription;
import by.mrj.server.service.data.ListService;
import by.mrj.server.service.data.LockingService;
import by.mrj.server.service.register.ClientRegister;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockingSender {

    private final DataProvider dataProvider;
    private final ClientRegister clientRegister;
    private final BasicClientSender basicClientSender;
    private final LockingService lockingService;
    private final ListService listService;

    /**
     * @param clientId
     * @return - {@code true} if data sent
     */
    public SendStatus sendAndRemove(String clientId) {

        if (!shouldISend(clientId)) {
            log.debug("Seems like another node job");
            return SendStatus.NO_ACTIVE_CHANNEL;
        }

        // todo: not sure needed.
        if (!lockingService.tryLock(clientId + HzConstants.Locks.USER_READ, 30)) {
            log.debug("Already sending to [{}]", clientId);
            return SendStatus.IN_PROGRESS;
        } else {
            log.debug("Locking sender for [{}]", clientId);
        }

        Map<String, List<Long>> sentIds;
        try {
            sentIds = basicClientSender.sendTo(clientId, (limit) -> dataProvider.getAllForUser(clientId, limit));

            int size = 0;
            for (Map.Entry<String, List<Long>> entry : sentIds.entrySet()) {
                String topic = entry.getKey();
                List<Long> ids = entry.getValue();
                size += ids.size();

                Subscription subscription = new Subscription(clientId, topic);

                listService.remove(subscription.mapName(), ids);
            }

            log.debug("Sent results size {}", size);

        } finally {
            log.debug("Unlocking sender for [{}]", clientId);
            lockingService.unlock(clientId + HzConstants.Locks.USER_READ);
        }

        if (sentIds.size() != 0 && isWebSocketConnection(clientId)) {
            return SendStatus.CONTINUE;
        }

        return SendStatus.OK;
    }

    private boolean isWebSocketConnection(String clientId) {
        DataClient dc = clientRegister.takeBest(clientId);

        ClientChannel streamingChannel = dc.getStreamingChannel();
        ConnectionInfo connectionInfo = streamingChannel.getConnectionInfo();

        return connectionInfo.getConnectionType() == ConnectionType.WS;
    }

    public SendStatus sendAndRemove(DataToSend data) {
        String clientId = data.getClientId();

        if (!shouldISend(clientId)) {
            log.info("Seems like another node job...");

            return SendStatus.NO_ACTIVE_CHANNEL;
        }

        if (!lockingService.tryLock(clientId + HzConstants.Locks.USER_READ)) {
            log.debug("Already sending to [{}]. Rollback size [{}]...", clientId, data.getIds().size());
            return SendStatus.IN_PROGRESS;
        } else {
            log.debug("Locking LS...");
        }

        try {
            Map<String, List<Long>> sentUuids = basicClientSender.sendTo(clientId, (limit) -> dataProvider.get(data.getTopicName(), data.getIds()),
                    (tn) -> HazelcastDataProvider.createSubsToIdsKey(clientId, tn.toUpperCase()));

//            dataProvider.removeFromMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS, sentUuids);
        } finally {
            log.debug("Unlocking LS...");
            lockingService.unlock(clientId + HzConstants.Locks.USER_READ);
        }

        return SendStatus.OK;
    }

    private boolean shouldISend(String clientId) {
        DataClient dataClient = clientRegister.takeBest(clientId);

        if (dataClient == DataClient.DUMMY) {
            log.info("No registered client [{}] found. Nothing will be sent", clientId);

            return false;
        }

        ClientChannel streamingChannel = dataClient.getStreamingChannel();

        if (streamingChannel == null || !streamingChannel.getChannel().isActive()) {
            log.debug("No active channel found for [{}]. Nothing will be sent", clientId);

            return false;
        }

        return true;
    }
}
