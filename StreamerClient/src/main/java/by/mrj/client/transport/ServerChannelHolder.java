package by.mrj.client.transport;

import by.mrj.common.domain.Message;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.client.ConnectionInfo;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class ServerChannelHolder {

    @Getter
    private ServerChannel channel;
    @Getter
    private ChannelFuture closeChannelFuture;

    public void createChannel(ClientChannelFactory clientChannelFactory, ConnectionInfo connectionInfo) {

        CompletableFuture.runAsync(() -> {

            EventLoopGroup group = new NioEventLoopGroup();
            try {
                channel = clientChannelFactory.createChannel(group, connectionInfo);

                log.info("[{}] connection established.", channel.getChannel());

                closeChannelFuture = channel.getChannel().closeFuture();

                closeChannelFuture.sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                group.shutdownGracefully();

                if (closeChannelFuture != null && !closeChannelFuture.isDone()) {
                    closeChannelFuture.cancel(true);
                }
            }
        });
    }

    public void send(Message<?> msg, MessageHeader messageHeader) {
        channel.send(msg, messageHeader);
    }

    public void closeFutureSync() {
        channel.closeFutureSync();
    }
}
