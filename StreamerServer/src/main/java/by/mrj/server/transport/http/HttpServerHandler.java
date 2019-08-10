package by.mrj.server.transport.http;

import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.channel.HttpStreamingChannel;
import by.mrj.common.domain.client.channel.LongPollingClientChannel;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.transport.converter.MessageChannelConverter;
import by.mrj.server.controller.CommandListener;
import by.mrj.domain.client.channel.ClientChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final CommandListener commandListener;
    private final MessageChannelConverter<FullHttpResponse> messageChannelConverter;
    private final DataSerializer serializer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        ByteBuf content = req.content();
        if (!content.isReadable()) {
            return; // todo: check ValidationHandler. Is this needed?
        }

        req.headers().get(HttpHeaderNames.AUTHORIZATION);

        int headerSize = content.readInt();
        String header = content.readCharSequence(headerSize, CharsetUtil.UTF_8).toString();
        String body = content.readCharSequence(content.readableBytes(), CharsetUtil.UTF_8).toString();

        ClientChannel streamChannel = createStreamChannel(ctx, req);

        commandListener.processRequest(header, body, streamChannel);
    }

    private ClientChannel createStreamChannel(ChannelHandlerContext ctx, FullHttpRequest req) {
        ConnectionType connectionType = ConnectionType.byUri(req.uri().substring(1));

        switch (connectionType) {
            case HTTP_STREAMING:
                return HttpStreamingChannel.from(ctx, serializer);
            case HTTP_LP:
                return LongPollingClientChannel.from(ctx, messageChannelConverter);
            default:
                throw new UnsupportedOperationException("Wrong connection type"); // fixme:

        }
    }
}
