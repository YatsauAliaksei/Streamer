package by.mrj.client.transport.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Base64;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


@Slf4j
@RequiredArgsConstructor
public class HttpSecurityServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        String auth = req.headers().get(HttpHeaderNames.AUTHORIZATION);

        if (!StringUtils.hasText(auth)) {
            log.error("Authorization header required.");
            return;
        }

        if (auth.startsWith(BEARER_PREFIX)) {
            boolean isOkay = checkJwt(auth.substring(7));
            if (isOkay) {
                req.retain();
                ctx.fireChannelRead(req);
            }
        } else if (auth.startsWith(BASIC_PREFIX)) {
            String[] loginAndPwd =
//                    new String(Base64.getDecoder()
//                    .decode(auth.substring(6)))
                    auth.substring(6)
                            .split(":");

            String login = loginAndPwd[0];
            String pwd = loginAndPwd[1];

            boolean isOkay = checkBasicAuth(login, pwd);
            if (isOkay) {
                String jwt = generateJwt(login);
                req.headers().add(HttpHeaderNames.AUTHORIZATION, BEARER_PREFIX + jwt);
                req.retain();
                ctx.fireChannelRead(req);
//                sendJwt(ctx.channel(), jwt);
            }
        } else
            throw new UnsupportedOperationException("Unknown authorization type [" + auth + "]");
    }

    private void sendJwt(Channel channel, String jwt) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(HttpHeaderNames.AUTHORIZATION, BEARER_PREFIX + jwt);

        channel.writeAndFlush(response);
    }

    /**
     * Generates new JWT
     */
    private String generateJwt(String login) {
        return "JWT-SECRET-TOKEN"; // todo:
    }

    /**
     * Checks is user authorized
     */
    private boolean checkBasicAuth(String login, String pwd) {
        return true; // todo:
    }

    /**
     * Checks JWT is valid
     */
    private boolean checkJwt(String jwtToken) {
        return true; // todo:
    }
}
