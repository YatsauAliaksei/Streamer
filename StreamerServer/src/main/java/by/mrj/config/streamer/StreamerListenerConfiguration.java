package by.mrj.config.streamer;

import by.mrj.controller.CommandListener;
import by.mrj.controller.StreamerController;
import by.mrj.domain.Message;
import by.mrj.domain.StreamingChannel;
import by.mrj.serialization.DataDeserializer;
import by.mrj.serialization.DataSerializer;
import by.mrj.serialization.json.JsonJackson;
import by.mrj.service.register.ClientRegister;
import by.mrj.service.register.InMemoryClientRegister;
import by.mrj.service.register.NewClientRegistrationListener;
import by.mrj.transport.converter.HttpMessageChannelConverter;
import by.mrj.transport.converter.MessageChannelConverter;
import by.mrj.transport.converter.WebSocketTextMessageChannelConverter;
import by.mrj.transport.websocket.server.WebSocketServer;
import by.mrj.transport.websocket.server.WebSocketServerInitializer;
import com.google.common.collect.Lists;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@Configuration
public class StreamerListenerConfiguration {

    @Value("${server.port}")
    private Integer port;

    @Bean
    @ConditionalOnProperty("ssl")
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
                                                                 MessageChannelConverter<String, ?> httpMessageChannelConverter,
                                                                 MessageChannelConverter<String, ?> wsMessageChannelConverter) {

        return new WebSocketServerInitializer(sslContext, commandListener, httpMessageChannelConverter, wsMessageChannelConverter);
    }

    @Bean
    public MessageChannelConverter<String, ?> wsMessageChannelConverter() {
        return new WebSocketTextMessageChannelConverter();
    }

    @Bean
    public MessageChannelConverter<String, ?> httpMessageChannelConverter(@Qualifier("dataSerializer") DataSerializer serializer) {
        return new HttpMessageChannelConverter(serializer);
    }

    @Bean
    public CommandListener commandListener(DataDeserializer dataDeserializer, ClientRegister clientRegister) {
        return new StreamerController(dataDeserializer, clientRegister);

    }

    @Bean
    public ClientRegister clientRegister() {
        List<Message<String>> messages = testData(1);

        return new InMemoryClientRegister(Lists.newArrayList((NewClientRegistrationListener) dataClient -> {

            StreamingChannel streamingChannel = dataClient.getStreamingChannel();
            messages.stream()
                    .peek(m -> log.debug("Sending message to client [{}]", m))
                    .map(JsonJackson::toJson)
                    .forEach(streamingChannel::write);

            log.info("Client registered [{}]", dataClient.getLoginName());

            streamingChannel.flush();

            streamingChannel.getChannel().close(); // in real world we shouldn't do that
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
