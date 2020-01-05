package by.mrj.client.transport;

import by.mrj.common.domain.Command;
import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.domain.data.BaseObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ServerChannelHolder {

    private final ServerChannel channel;
    private boolean isConnected;

    public ChannelFuture readAll() {
        return channel.send(
                Message.<String>builder()
                        .payload("Read all Operation")
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.READ_ALL)
                        .build());
    }

    public ChannelFuture post(List<BaseObject> data) {
        return channel.send(data);
    }

    public ChannelFuture subscribe(List<String> subscriptions) {
        return channel.send(
                Message.<String[]>builder()
                        .payload(subscriptions.toArray(new String[0]))
                        .build(),
                MessageHeader
                        .builder()
                        .command(Command.SUBSCRIBE)
                        .build());
    }

    public ChannelFuture authorize(String login, String pwd) {
        log.info("Authorizing client...");

        return channel.authorize(login, pwd);
    }

    public ChannelFuture send(Message<?> msg, MessageHeader messageHeader) {
        if (!isConnected) {
            log.info("Not connected yet...");
            return channel.getChannel().newSucceededFuture();
        }

        return channel.send(msg, messageHeader);
    }

    public ChannelFuture send(List<BaseObject> postData) {
        if (!isConnected) {
            log.info("Not connected yet...");
            return channel.getChannel().newSucceededFuture();
        }

        return channel.send(postData);
    }

    public static Single<ServerChannelHolder> create(ClientChannelFactory clientChannelFactory, ConnectionInfo connectionInfo) {
        return create(clientChannelFactory, connectionInfo, null);
    }

    public static Single<ServerChannelHolder> create(ClientChannelFactory clientChannelFactory,
                                                     ConnectionInfo connectionInfo,
                                                     ChannelFutureListener channelFutureListener) {

        if (channelFutureListener != null) {
            clientChannelFactory.setHandshakeListener(channelFutureListener);
        }

        return Single.create(emitter ->
                new Thread(() -> {
//                CompletableFuture.runAsync(() -> {
                    ChannelFuture closeChannelFuture = null;
                    EventLoopGroup group = new NioEventLoopGroup();

                    ServerChannelHolder sch;
                    try {
                        log.debug("Creating channel [{}]", connectionInfo.getConnectionType());

                        ServerChannel channel = clientChannelFactory.createChannel(group, connectionInfo);
                        sch = new ServerChannelHolder(channel);

                        sch.isConnected = true;

//                        sch.authorize(connectionInfo.getLogin(), connectionInfo.getPassword());

                        emitter.onSuccess(sch);

                        log.debug("Connection established [{}].", sch.channel.getChannel());

                        closeChannelFuture = sch.channel.getChannel().closeFuture();
                        closeChannelFuture.sync();

                    } catch (InterruptedException e) {
                        emitter.onError(e); // todo:
                        throw new RuntimeException(e);
                    } finally {
                        group.shutdownGracefully();

                        if (closeChannelFuture != null && !closeChannelFuture.isDone()) {
                            closeChannelFuture.cancel(true);
                        }
                    }
//                }).exceptionally(throwable -> {
//                    throw new RuntimeException(throwable);
//                }));
                }).start());
    }

    public Channel rawChannel() {
        return channel.getChannel();
    }

    public void closeFutureSync() {
        channel.closeFutureSync();
        isConnected = false;
    }
}
