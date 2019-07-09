package by.mrj.config.streamer;

import by.mrj.controller.CommandListener;
import by.mrj.controller.StreamerController;
import by.mrj.data.DataProvider;
import by.mrj.data.kafka.adapter.KafkaDataProviderAdapter;
import by.mrj.data.kafka.provider.KafkaDataProvider;
import by.mrj.domain.Message;
import by.mrj.domain.StreamingChannel;
import by.mrj.serialization.DataDeserializer;
import by.mrj.serialization.DataSerializer;
import by.mrj.serialization.json.JsonJackson;
import by.mrj.service.DataDispatcher;
import by.mrj.service.register.ClientRegister;
import by.mrj.service.register.InMemoryClientRegister;
import by.mrj.service.register.NewClientRegistrationListener;
import by.mrj.transport.converter.MessageChannelConverter;
import by.mrj.transport.converter.binary.HttpByteMessageChannelConverter;
import by.mrj.transport.converter.binary.WebSocketByteMessageChannelConverter;
import by.mrj.transport.converter.text.HttpTextMessageChannelConverter;
import by.mrj.transport.converter.text.WebSocketTextMessageChannelConverter;
import by.mrj.transport.websocket.server.WebSocketServer;
import by.mrj.transport.websocket.server.WebSocketServerInitializer;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
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

import static io.netty.handler.codec.http.HttpHeaderNames.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderValues.CHUNKED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


@Slf4j
@Configuration
public class StreamerListenerConfiguration {

    @Value("${streamer.port}")
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
                                                                 MessageChannelConverter<String, ?> wsMessageChannelConverter,
                                                                 MessageChannelConverter<ByteBuf, ?> httpByteMessageChannelConverter,
                                                                 MessageChannelConverter<ByteBuf, ?> wsByteMessageChannelConverter) {

        return new WebSocketServerInitializer(sslContext, commandListener,
                httpMessageChannelConverter, wsMessageChannelConverter,
                httpByteMessageChannelConverter, wsByteMessageChannelConverter);
    }

    @Bean
    public MessageChannelConverter<String, ?> wsMessageChannelConverter() {
        return new WebSocketTextMessageChannelConverter();
    }

    @Bean
    public MessageChannelConverter<String, ?> httpMessageChannelConverter(@Qualifier("dataSerializer") DataSerializer serializer) {
        return new HttpTextMessageChannelConverter(serializer);
    }

    @Bean
    public MessageChannelConverter<ByteBuf, ?> httpByteMessageChannelConverter() {
        return new HttpByteMessageChannelConverter();
    }

    @Bean
    public MessageChannelConverter<ByteBuf, ?> wsByteMessageChannelConverter() {
        return new WebSocketByteMessageChannelConverter();
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
//            streamingChannel.write("yeap");
//            streamingChannel.flush();

            messages.stream()
                    .peek(m -> log.debug("Sending message to client [{}]", m))
                    .map(JsonJackson::toJson)
                    .forEach(m -> {
                        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
                        response.headers().set(TRANSFER_ENCODING, CHUNKED);
                        streamingChannel.getChannel().write(response);
                    });

            log.info("Client registered [{}]", dataClient.getLoginName());

            streamingChannel.flush();
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

    @Bean
    public DataProvider<ByteBuf> dataProvider(@Qualifier("dataSerializer") DataSerializer serializer) {
        return new KafkaDataProvider(serializer);
    }

    @Bean
    public KafkaDataProviderAdapter<ByteBuf> dataProviderAdapter(DataProvider<ByteBuf> dataProvider) {
        return new KafkaDataProviderAdapter<>(dataProvider);
    }

    @Bean
    public DataDispatcher dataDispatcher(ClientRegister clientRegister, KafkaDataProviderAdapter<ByteBuf> dataProviderAdapter) {
        return new DataDispatcher(clientRegister, dataProviderAdapter);
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
