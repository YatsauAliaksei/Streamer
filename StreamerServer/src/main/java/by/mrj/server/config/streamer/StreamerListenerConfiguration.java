package by.mrj.server.config.streamer;

import by.mrj.common.serialization.DataDeserializer;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.serialization.json.JsonJackson;
import by.mrj.common.transport.converter.MessageChannelConverter;
import by.mrj.server.config.ApplicationProperties;
import by.mrj.server.controller.CommandListener;
import by.mrj.server.security.jwt.JWTFilter;
import by.mrj.server.service.merkletree.MerkleTreeService;
import by.mrj.server.service.register.ClientRegister;
import by.mrj.server.service.register.InMemoryClientRegister;
import by.mrj.server.service.register.NewClientRegistrationListener;
import by.mrj.server.transport.ServerInitializer;
import by.mrj.server.transport.SocketServer;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.List;


@Slf4j
@Configuration
@ComponentScan(basePackages = "by.mrj.server")
@EnableAutoConfiguration
@EnableConfigurationProperties({ApplicationProperties.class})
@EnableMBeanExport
public class StreamerListenerConfiguration {

    @Value("${streamer.port}")
    private Integer port;
    @Value("${streamer.topic.initSize}")
    private Integer topicInitSize;

    @Bean
    @ConditionalOnProperty(value = "server.ssl", havingValue = "true")
    public SslContext sslContext() throws CertificateException, SSLException {
        // Configure SSL.
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }

    @Bean
    public SocketServer webSocketServer(ServerInitializer serverInitializer) {
        SocketServer socketServer = new SocketServer(serverInitializer);
        socketServer.setPort(port);
        socketServer.listen();
        return socketServer;
    }

    @Bean
    public MerkleTreeService merkleTreeService() {
        return new MerkleTreeService(topicInitSize);
    }

    // todo: to many dependencies. Smells
    @Bean
    public ServerInitializer serverInitializer(@Autowired(required = false) SslContext sslContext,
                                               CommandListener commandListener,
                                               @Qualifier("dataSerializer") DataSerializer serializer,
                                               JWTFilter jwtFilter,
                                               MessageChannelConverter<FullHttpResponse> httpMessageChannelConverter,
                                               MessageChannelConverter<WebSocketFrame> wsMessageChannelConverter) {

        return new ServerInitializer(sslContext, commandListener,
                httpMessageChannelConverter, wsMessageChannelConverter, serializer, jwtFilter);
    }

    @Bean
    public ClientRegister clientRegister(List<NewClientRegistrationListener> clientRegistrationListenerList) {
        return new InMemoryClientRegister(clientRegistrationListenerList);
    }

    @Bean
    public NewClientRegistrationListener clientRegistrationListener() {
        return dataClient ->
                log.debug("Client registered [{}]", dataClient.getId());
    }

    @Bean
    public DataDeserializer dataDeserializer() {
        return new JsonJackson();
    }

    @Bean
    public DataSerializer dataSerializer() {
        return new JsonJackson();
    }
}
