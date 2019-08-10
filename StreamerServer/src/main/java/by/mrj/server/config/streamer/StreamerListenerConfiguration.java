package by.mrj.server.config.streamer;

import by.mrj.client.transport.websocket.server.WebSocketServer;
import by.mrj.common.domain.Message;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.serialization.json.JsonJackson;
import by.mrj.common.transport.converter.MessageChannelConverter;
import by.mrj.server.controller.CommandListener;
import by.mrj.server.service.register.ClientRegister;
import by.mrj.server.service.register.InMemoryClientRegister;
import by.mrj.server.service.register.NewClientRegistrationListener;
import by.mrj.server.transport.websocket.server.WebSocketServerInitializer;
import com.google.common.collect.Lists;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@Configuration
@ComponentScan("by.mrj.server")
public class StreamerListenerConfiguration {

    @Value("${streamer.port}")
    private Integer port;

    @Bean
    @ConditionalOnProperty(value = "server.ssl", havingValue = "true")
    public SslContext sslContext() throws CertificateException, SSLException {
        // Configure SSL.
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }

    @Bean
    public WebSocketServer webSocketServer(WebSocketServerInitializer webSocketServerInitializer) {
        WebSocketServer webSocketServer = new WebSocketServer(webSocketServerInitializer);
        webSocketServer.setPort(port);
        webSocketServer.listen();
        return webSocketServer;
    }

    @Bean
    public WebSocketServerInitializer webSocketServerInitializer(@Autowired(required = false) SslContext sslContext,
                                                                 CommandListener commandListener,
                                                                 @Qualifier("dataSerializer") DataSerializer serializer,
                                                                 MessageChannelConverter<FullHttpResponse> httpMessageChannelConverter,
                                                                 MessageChannelConverter<WebSocketFrame> wsMessageChannelConverter) {

        return new WebSocketServerInitializer(sslContext, commandListener,
                httpMessageChannelConverter, wsMessageChannelConverter, serializer);
    }

    @Bean
    public ClientRegister clientRegister() {
//        List<Message<String>> messages = testData(1);

        return new InMemoryClientRegister(Lists.newArrayList((NewClientRegistrationListener) dataClient -> {

//            ClientChannel streamingChannel = dataClient.getStreamingChannel();
//            streamingChannel.write("yeap");
//            streamingChannel.flush();

/*            messages.stream()
                    .peek(m -> log.debug("Sending message to client [{}]", m))
                    .map(JsonJackson::toJson)
                    .forEach(m -> {
                        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
                        response.headers().set(TRANSFER_ENCODING, CHUNKED);
                        streamingChannel.getChannel().write(response);
                    });*/

            log.info("Client registered [{}]", dataClient.getLoginName());

//            streamingChannel.flush();
            // NOOP
        }));
    }

    @Bean
    public DataDeserializer dataDeserializer() {
        return new JsonJackson();
    }

    @Bean
    public DataSerializer dataSerializer() {
        return new JsonJackson();
    }

    private static List<Message<String>> testData(int size) {
        return IntStream.range(0, size)
                .boxed()
                .map(k -> Message.<String>builder()
                        .payload("Hi-" + k)
                        .build())
                .collect(Collectors.toList());
    }
}
