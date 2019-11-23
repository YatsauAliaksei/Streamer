package by.mrj.common.domain.client.channel;


import by.mrj.common.domain.Command;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.MessageHeader;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

import static io.netty.handler.codec.http.HttpHeaderNames.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderValues.CHUNKED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


@Slf4j
@Getter
@ToString
public class HttpStreamingChannel implements ClientChannel {

    // fixme: totally wrong
    private final Queue<ByteBuf> chunks = new ArrayDeque<>();

    private final Channel channel;
    private final ConnectionInfo connectionInfo;
    private final DataSerializer serializer;

    public HttpStreamingChannel(Channel channel, ConnectionInfo connectionInfo, DataSerializer serializer) {
        this.channel = channel;
        this.connectionInfo = connectionInfo;
        this.serializer = serializer;

        sendStreamingHeader(channel);
    }

    public void writeAndFlush(String toSend) {
        this.write(toSend);
        this.flush();
    }

    public void writeAndFlush(ByteBuf toSend) {
        this.write(toSend);
        this.flush();
    }

    public void write(String toSend) {
        ByteBuf msgBuf = ByteBufUtils.create(serializer,
                MessageHeader.builder()
                        .command(Command.POST)
                        .build(),
                by.mrj.common.domain.Message.<String>builder()
                        .payload(toSend)
                        .build());

        this.write(msgBuf);
    }

    public void write(ByteBuf toSend) {
        chunks.add(toSend);
    }

    public void flush() {
        channel.flush();
    }

    private void sendStreamingHeader(Channel channel) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.headers().set(TRANSFER_ENCODING, CHUNKED);
        getChannel().write(response);
        getChannel().flush();

        HttpChunkedInput httpChunkWriter = new HttpChunkedInput(new ChunkedStream(
                new ByteArrayInputStream("".getBytes()), Integer.MAX_VALUE)) {

            @Override
            public boolean isEndOfInput() throws Exception {
                log.debug("Not yet end of chunks...");
                return false;
            }

            @Override
            public HttpContent readChunk(ByteBufAllocator allocator) throws Exception {
                log.debug("Read chunk...");
                if (chunks.isEmpty()) {
                    log.debug("No chunks found");
                    return null;
                }

                ByteBuf buf = chunks.poll();
                if (buf == null) {
                    log.debug("Null BB polled");
                    return null;
                }

                buf.retain();

                log.debug("Read chunk [{}]", buf.toString(CharsetUtil.UTF_8));
                return new DefaultHttpContent(buf);
            }
        };

        ChannelFuture sendFileFuture = channel.writeAndFlush(httpChunkWriter);

        sendFileFuture.addListener(future -> {
            if (!future.isSuccess()) {
                log.error("Failed to send header chunk", future.cause());
            }
        });
    }

    public static HttpStreamingChannel from(ChannelHandlerContext ctx, DataSerializer serializer) {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        String host = null;
        Integer port = null;
        if (socketAddress instanceof InetSocketAddress) {
            host = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
            port = ((InetSocketAddress) socketAddress).getPort();
        }

        ConnectionInfo connectionInfo = ConnectionInfo.from(ConnectionType.HTTP_STREAMING, null, host, port);

        return new HttpStreamingChannel(ctx.channel(), connectionInfo, serializer);
    }
}
