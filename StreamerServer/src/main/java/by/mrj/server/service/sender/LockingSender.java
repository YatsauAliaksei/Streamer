package by.mrj.server.service.sender;

import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.client.channel.ClientChannel;
import by.mrj.server.data.DataProvider;
import by.mrj.server.data.HazelcastDataProvider;
import by.mrj.server.data.HzConstants;
import by.mrj.server.data.domain.DataToSend;
import by.mrj.server.data.domain.SendStatus;
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

    /**
     * @param clientId
     * @return - {@code true} if data sent
     */
    public boolean sendAndRemove(String clientId) {

        if (!shouldISend(clientId)) {
            log.debug("Seems like another node job...");
            return false;
        }

        // todo: not sure needed.
        if (!dataProvider.tryLock(clientId + HzConstants.Locks.USER_READ)) {
            log.debug("Already sending to [{}]...", clientId);
            return false;
        } else {
            log.debug("Locking sender...");
        }

        try {
            Map<String, List<String>> sentUuids = basicClientSender.sendTo(clientId, (limit) -> dataProvider.getAllForUser(clientId, limit),
                    (tn) -> HazelcastDataProvider.createSubsToIdsKey(clientId, tn.toUpperCase()));

//            dataProvider.removeFromMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS, sentUuids);
        } finally {
            log.debug("Unlocking sender...");
            dataProvider.unlock(clientId + HzConstants.Locks.USER_READ);
        }

        return true;
    }

    public SendStatus sendAndRemove(DataToSend data) {
        String clientId = data.getClientId();

        if (!shouldISend(clientId)) {
            log.info("Seems like another node job...");

            return SendStatus.NO_ACTIVE_CHANNEL;
        }

        if (!dataProvider.tryLock(clientId + HzConstants.Locks.USER_READ)) {
            log.debug("Already sending to [{}]. Rollback size [{}]...", clientId, data.getUuids().size());
            return SendStatus.IN_PROGRESS;
        } else {
            log.debug("Locking LS...");
        }

        try {
            Map<String, List<String>> sentUuids = basicClientSender.sendTo(clientId, (limit) -> dataProvider.get(data.getTopicName(), data.getUuids()),
                    (tn) -> HazelcastDataProvider.createSubsToIdsKey(clientId, tn.toUpperCase()));

//            dataProvider.removeFromMultiMap(HzConstants.Maps.SUBSCRIPTION_TO_IDS, sentUuids);
        } finally {
            log.debug("Unlocking LS...");
            dataProvider.unlock(clientId + HzConstants.Locks.USER_READ);
        }

        return SendStatus.OK;
    }

    private boolean shouldISend(String clientId) {
        DataClient dataClient = clientRegister.findBy(clientId);

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
