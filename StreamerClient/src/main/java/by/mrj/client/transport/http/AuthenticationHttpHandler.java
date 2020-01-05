package by.mrj.client.transport.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.reactivex.Single;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationHttpHandler extends SimpleChannelInboundHandler<HttpResponse> {

    private BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);

    @Getter
    private Single<String> promise = Single.create(emitter ->
            CompletableFuture.runAsync(() -> {

                String jwt;
                try {
                    jwt = queue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                emitter.onSuccess(jwt);
            }));

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpResponse response) throws Exception {
        log.debug("LongPolling Http Client received message: [{}]", response);

        String jwt = response.headers().get(HttpHeaderNames.AUTHORIZATION);

        if (StringUtils.hasText(jwt)) {
            ctx.pipeline().remove(this);

            log.debug("Adding to queue [{}]", jwt);

            queue.add(jwt);
            return;
        }

        // todo: what to do if not?
        log.warn("First request should be authorization request");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause != null) {
            log.error("Error processing response", cause);
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public String toString() {
        return "Authentication handler";
    }
}
