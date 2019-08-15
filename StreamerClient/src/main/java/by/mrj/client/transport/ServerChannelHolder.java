package by.mrj.client.transport;

import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.client.ConnectionInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.reactivex.Single;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@ToString//(exclude = "closeChannelFuture")
public class ServerChannelHolder {

    @Getter
    private volatile ServerChannel channel;
    //    @Getter
//    private ChannelFuture closeChannelFuture;
    @Getter
    private boolean isConnected;
    @Getter
    private boolean isClosed; // todo: think about sync

    public Single<? extends ServerChannel> createChannel(ClientChannelFactory clientChannelFactory, ConnectionInfo connectionInfo, ChannelFutureListener channelFutureListener) {

        if (isClosed) {
            throw new RuntimeException("Channel holder can't be reused. Create new instead"); // todo: check
        }

        if (channelFutureListener != null) {
            clientChannelFactory.setHandshakeListener(channelFutureListener);
        }

        return Single.create(emitter ->
                CompletableFuture.runAsync(() -> {
                    ChannelFuture closeChannelFuture = null;
                    EventLoopGroup group = new NioEventLoopGroup();

                    try {
                        channel = clientChannelFactory.createChannel(group, connectionInfo);
                        isConnected = true;

                        emitter.onSuccess(channel);

                        log.info("[{}] connection established.", channel.getChannel());

                        closeChannelFuture = channel.getChannel().closeFuture();
                        closeChannelFuture.sync();

                    } catch (InterruptedException e) {
                        emitter.onError(e); // todo:
                        throw new RuntimeException(e);
                    } finally {
                        group.shutdownGracefully();

                        if (closeChannelFuture != null && !closeChannelFuture.isDone()) {
                            closeChannelFuture.cancel(true);
                        }
                        isClosed = true;
                    }
                }));
    }

    public Single<? extends ServerChannel> createChannel(ClientChannelFactory clientChannelFactory, ConnectionInfo connectionInfo) {
        return createChannel(clientChannelFactory, connectionInfo, null);
    }

    public Channel rawChannel() {
        return channel.getChannel();
    }

    public void send(Message<?> msg, MessageHeader messageHeader) {
        if (!isConnected) {
            log.info("Not connected yet...");
            return;
        }

        channel.send(msg, messageHeader);
    }

    public void closeFutureSync() {
        channel.closeFutureSync();
        isConnected = false;
        isClosed = true;
    }
}
