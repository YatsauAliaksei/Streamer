package by.mrj.transport;

import by.mrj.controller.CommandListener;
import by.mrj.domain.ConnectionType;
import by.mrj.domain.StreamingChannel;
import by.mrj.transport.converter.MessageChannelConverter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
@RequiredArgsConstructor
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final CommandListener commandListener;
    private final MessageChannelConverter<String, ?> messageChannelConverter;
    private final MessageChannelConverter<ByteBuf, ?> byteMessageChannelConverter;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        if (!req.decoderResult().isSuccess()) {
            log.debug("Bad request [{}]", req);
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (!HttpMethod.GET.equals(req.method())) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        ByteBuf content = req.content();
        if (!content.isReadable()) {
            return;
        }

        log.debug("Bytes to read: [{}]", content.readableBytes());

        int headerSize = content.readInt();
        String header = content.readCharSequence(headerSize, CharsetUtil.UTF_8).toString();
        String body = content.readCharSequence(content.readableBytes(), CharsetUtil.UTF_8).toString();

        commandListener.processRequest(header, body,
                StreamingChannel.from(ctx, "http", messageChannelConverter, byteMessageChannelConverter));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Http request processing failed", cause);
        ctx.close();
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, FullHttpResponse res) {
        // Send the response and close the connection if necessary.
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            // Tell the client we're going to close the connection.
            res.headers().set(CONNECTION, CLOSE);
            ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
        } else {
            if (req.protocolVersion().equals(HTTP_1_0)) {
                res.headers().set(CONNECTION, KEEP_ALIVE);
            }
            ctx.writeAndFlush(res);
        }
    }
}
