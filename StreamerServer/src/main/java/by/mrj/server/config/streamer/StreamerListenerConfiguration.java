package by.mrj.server.config.streamer;

import by.mrj.common.serialization.DataDeserializer;
import by.mrj.common.serialization.DataSerializer;
import by.mrj.common.serialization.json.JsonJackson;
import by.mrj.common.transport.converter.MessageChannelConverter;
import by.mrj.server.config.ApplicationProperties;
import by.mrj.server.controller.CommandListener;
import by.mrj.server.security.jwt.JWTFilter;
import by.mrj.server.service.register.ClientRegister;
import by.mrj.server.service.register.InMemoryClientRegister;
import by.mrj.server.service.register.NewClientRegistrationListener;
import by.mrj.server.transport.ServerInitializer;
import by.mrj.server.transport.SocketServer;
import com.google.common.collect.Lists;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import java.util.Properties;


@Slf4j
@Configuration
@ComponentScan(basePackages = "by.mrj.server")
@EnableAutoConfiguration
@EnableConfigurationProperties({ApplicationProperties.class})
@EnableMBeanExport
public class StreamerListenerConfiguration {

    @Value("${streamer.port}")
    private Integer port;
    @Value("${streamer.kafka.url}")
    private String kafkaServerUrl;
    @Value("${streamer.kafka.port}")
    private String kafkaServerPort;
    @Value("${streamer.kafka.client.id}")
    private String clientId;


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

//    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//    public GeneralTopic generalTopic() {
//
//    }

    @Bean
    public ClientRegister clientRegister(List<NewClientRegistrationListener> clientRegistrationListenerList) {
        return new InMemoryClientRegister(clientRegistrationListenerList);
    }

    @Bean
    public NewClientRegistrationListener clientRegistrationListener() {
        return dataClient ->
                log.info("Client registered [{}]", dataClient.getId());
    }

    //    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerUrl + ":" + kafkaServerPort);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<>("", "key", "value"));
        return producer;
    }

    //    @Bean
    public KafkaConsumer<String, String> kafkaConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerUrl + ":" + kafkaServerPort);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, clientId);
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
//        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        return consumer;
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
