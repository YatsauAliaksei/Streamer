package by.mrj.server.config.streamer;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICacheManager;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class HazelcastClientConfiguration implements DisposableBean {

    @Override
    public void destroy() throws Exception {
        log.info("Closing Cache Manager");
        HazelcastClient.shutdownAll();
    }

/*    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
        log.info("Starting HazelcastCacheManager");
        return new HazelcastCacheManager(hazelcastInstance);
    }*/

    @Bean
    public ICacheManager cacheManager(HazelcastInstance hazelcastInstance) {
        log.info("Starting HazelcastCacheManager");

        return hazelcastInstance.getCacheManager();
    }

    @Bean
    public HazelcastInstance hazelcastInstance(JHipsterProperties jHipsterProperties, ClientConfig hzClientConfig) {
        log.info("Configuring Hazelcast");

//        ClientNetworkConfig config = new ClientNetworkConfig();

        return HazelcastClient.newHazelcastClient(hzClientConfig);
    }

    @Bean // todo: not sure needed as a bean
    public ClientConfig hzClientConfig() {
        ClientConfig config = new ClientConfig();
        config.getGroupConfig().setName("hz-group");//.setPassword("dev-pass");
        config.addAddress("127.0.0.1:31375");

        return config;
    }
}
