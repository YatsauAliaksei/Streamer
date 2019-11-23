package by.mrj.server.transport.http;

import by.mrj.server.security.jwt.JWTFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;


@Slf4j
@RequiredArgsConstructor
public class HttpSecurityServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final JWTFilter jwtFilter;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        jwtFilter.authorize(req);

        if (SecurityContextHolder.getContext() == null) {
            log.error("Not authorized access detected.");
            return;
        }

        req.retain();
        ctx.fireChannelRead(req);
    }
}
