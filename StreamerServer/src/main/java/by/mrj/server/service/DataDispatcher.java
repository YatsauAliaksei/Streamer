package by.mrj.server.service;

import by.mrj.common.domain.client.DataClient;
import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;
import by.mrj.server.data.kafka.adapter.KafkaDataProviderAdapter;
import by.mrj.server.service.register.ClientRegister;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
//@ManagedResource
@RequiredArgsConstructor
public class DataDispatcher {

    private final ClientRegister clientRegister;
    private final KafkaDataProviderAdapter<ByteBuf> kafkaDataProviderAdapter;

    @ManagedOperation(description = "Sends All available data to client")
    public void sendAllAvailableDataTo(String identifier) {
        DataClient client = clientRegister.findBy(identifier);

        if (client == DataClient.DUMMY) {
            log.error("No client data found for id [{}]", identifier);
            return;
        }

        if (!client.getStreamingChannel().getChannel().isActive()) {
            log.error("Channel closed for [{}]", client.getId());
            return;
        }

        List<BaseObject> byteBufs = kafkaDataProviderAdapter.allAvailableData(identifier);

        log.debug("Sending to [{}] {} messages", identifier, byteBufs.size());
        log.debug("Messages [{}]", byteBufs);

        byteBufs.forEach(bb -> {
//            bb.resetReaderIndex();
            client.getStreamingChannel().writeAndFlush(JsonJackson.toJson(bb));
        });

        log.debug("Data for [{}] was sent", identifier);
    }
}
