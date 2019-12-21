package by.mrj.server.transport.http;

import by.mrj.server.security.jwt.JWTFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;


@Slf4j
@RequiredArgsConstructor
public class HttpSecurityServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final JWTFilter jwtFilter;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        String jwt = jwtFilter.authorize(req);

        if (SecurityContextHolder.getContext() == null
                || SecurityContextHolder.getContext().getAuthentication() == null) {

            log.error("Not authorized access detected.");
            req.release(req.refCnt());
            return;
        }

        req.headers().set(HttpHeaderNames.AUTHORIZATION, "Bearer " + jwt);

        req.retain();
        ctx.fireChannelRead(req);
    }
}
