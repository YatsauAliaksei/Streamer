package by.mrj.client.config.streamer;

import by.mrj.client.config.ApplicationProperties;
import by.mrj.client.connection.ConnectionHolder;
import by.mrj.client.connection.ConnectionInfoFactory;
import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.service.MessageLoggingConsumer;
import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.serialization.json.JsonJackson;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
//@SpringBootApplication
@Configuration
@ComponentScan("by.mrj.client")
@EnableConfigurationProperties({ApplicationProperties.class})
@EnableAutoConfiguration
//@EnableConfigurationProperties({ApplicationProperties.class})
//@ComponentScan(excludeFilters = {
//        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
//        @ComponentScan.Filter(type = FilterType.CUSTOM,
//                classes = AutoConfigurationExcludeFilter.class) })
public class StreamerClientConfiguration {

    @Bean
    @ConditionalOnProperty(value = "streamer.ssl", havingValue = "true")
    public SslContext sslContext() throws CertificateException, SSLException {
        // Configure SSL.
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }

    @Bean
    @Profile("dev")
    public MessageLoggingConsumer messageConsumer(ApplicationEventPublisher publisher) {
        return new MessageLoggingConsumer(publisher);
    }

    @Bean
    public ConnectionManager dummyConnectionManager(List<ClientChannelFactory> clientChannelFactories,
                                                    ConnectionInfoFactory autoConnectionInfoFactory,
                                                    ConnectionHolder connectionHolder) {

        Map<ConnectionType, ClientChannelFactory> connectionTypeToClientChannelFactory = clientChannelFactories.stream()
                .collect(Collectors.toMap(ClientChannelFactory::connectionType, Function.identity()));

        ConnectionManager dummyConnectionManager = new ConnectionManager(connectionTypeToClientChannelFactory,
                autoConnectionInfoFactory, connectionHolder);

//        dummyConnectionManager.autoConnect();
        log.info("Connection manager created.");

        return dummyConnectionManager;
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
