package by.mrj.client.config.streamer;

import by.mrj.client.connection.AutoConnectionInfoFactory;
import by.mrj.client.connection.ConnectionManager;
import by.mrj.client.transport.ClientChannelFactory;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.serialization.DataDeserializer;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.serialization.json.JsonJackson;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Configuration
@ComponentScan("by.mrj.client")
public class StreamerClientConfiguration {

    @Bean
    @ConditionalOnProperty(value = "streamer.ssl", havingValue = "true")
    public SslContext sslContext() throws CertificateException, SSLException {
        // Configure SSL.
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    }

    @Bean
    public ConnectionManager dummyConnectionManager(List<ClientChannelFactory> clientChannelFactories,
                                                    AutoConnectionInfoFactory autoConnectionInfoFactory) {

        Map<ConnectionType, ClientChannelFactory> connectionTypeToClientChannelFactory = clientChannelFactories.stream()
                .collect(Collectors.toMap(ClientChannelFactory::connectionType, Function.identity()));

        ConnectionManager dummyConnectionManager = new ConnectionManager(connectionTypeToClientChannelFactory,
                autoConnectionInfoFactory);

        dummyConnectionManager.autoConnect();

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
