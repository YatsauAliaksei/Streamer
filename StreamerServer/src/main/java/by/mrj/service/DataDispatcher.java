package by.mrj.service;

import by.mrj.data.kafka.adapter.KafkaDataProviderAdapter;
import by.mrj.domain.client.DataClient;
import by.mrj.service.register.ClientRegister;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ManagedResource
@RequiredArgsConstructor
public class DataDispatcher {

    private final ClientRegister clientRegister;
    private final KafkaDataProviderAdapter<ByteBuf> kafkaDataProviderAdapter;

    private Set<String> headerSent = new HashSet<>();

    @ManagedOperation(description = "Sends All available data to client")
    public void sendAllAvailableDataTo(String identifier) {
        DataClient client = clientRegister.findBy(identifier);

        if (client == DataClient.DUMMY) {
            return;
        }

        if (!client.getStreamingChannel().getChannel().isActive()) {
            log.error("Channel closed for [{}]", client.getLoginName());
        }

        List<ByteBuf> byteBufs = kafkaDataProviderAdapter.allAvailableData(identifier);

        log.debug("Sending to [{}] {} messages", identifier, byteBufs.size());
        log.debug("Messages [{}]", byteBufs);

        if (!headerSent.contains(identifier)) {
            ChannelFuture sendFileFuture = client.getStreamingChannel().getChannel().writeAndFlush(httpChunkWriter);

            sendFileFuture.addListener(future -> {
                if(!future.isSuccess()) {
                    log.error("Failed to send chunk", future.cause());
                }
            });
            headerSent.add(identifier);
        }

        byteBufs.forEach(bb -> {
            bb.resetReaderIndex();
            queue.add(bb);
            client.getStreamingChannel().flush();
        });

        log.debug("Data for [{}] was sent", identifier);
    }

    Queue<ByteBuf> queue = new ArrayDeque<>();

    HttpChunkedInput httpChunkWriter = new HttpChunkedInput(new ChunkedStream(new ByteArrayInputStream("".getBytes()), Integer.MAX_VALUE)) {

        @Override
        public boolean isEndOfInput() throws Exception {
            log.debug("Not yet end of chunks...");
            return false;
        }

        @Override
        public HttpContent readChunk(ByteBufAllocator allocator) throws Exception {
            log.debug("Read chunk...");
            if (queue.isEmpty()) {
                log.debug("No chunks found");
                return null;
            }

            ByteBuf buf = queue.poll();
            if (buf == null) {
                log.debug("Null BB polled");
                return null;
            }

            buf.retain();

            log.debug("Read chunk [{}]", buf.toString(CharsetUtil.UTF_8));
            return new DefaultHttpContent(buf);
        }
    };


}
