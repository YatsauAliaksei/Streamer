package by.mrj.server.transport;

import by.mrj.client.transport.PortListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;


@Slf4j
@RequiredArgsConstructor
public class SocketServer implements PortListener {

    private final ChannelHandler serverChannelInitializer;
    @Setter
    private Integer port;

    @Override
//    @SneakyThrows
    public void listen() {
        CompletableFuture.runAsync(() -> {

            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        // todo: make it flexible in case Linux/Epoll possible
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .childHandler(serverChannelInitializer);

                Channel ch = b.bind(port).sync().channel();

                log.info("Listening on port {}", port);
    //            log.info("Open your web browser and navigate to " +
    //                    (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

                // todo: close it when needed. F.i. when app stopped.
                ch.closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
    }

    @Override
    public String toString() {
        return "Netty NIO WebSocket listener";
    }
}
