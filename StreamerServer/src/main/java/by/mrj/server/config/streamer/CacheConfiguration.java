package by.mrj.server.config.streamer;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICacheManager;
import io.github.jhipster.config.JHipsterConstants;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

//@Configuration
@Log4j2
//@EnableCaching
public class CacheConfiguration implements DisposableBean {

    private final Environment env;

    public CacheConfiguration(Environment env) {
        this.env = env;
    }

    @Override
    public void destroy() throws Exception {
        log.info("Closing Cache Manager");
        Hazelcast.shutdownAll();
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
    public HazelcastInstance hazelcastInstance(JHipsterProperties jHipsterProperties) {
        log.info("Configuring Hazelcast");

        HazelcastInstance hazelCastInstance = Hazelcast.getHazelcastInstanceByName("Streamer");
        if (hazelCastInstance != null) {
            log.info("Hazelcast already initialized");

            return hazelCastInstance;
        }

        Config config = new Config();
        config.setInstanceName("Streamer");
        config.getNetworkConfig().setPort(5701);
        config.getNetworkConfig().setPortAutoIncrement(true);

        // In development, remove multicast auto-configuration
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))) {
            log.info("Hazelcast DEV local run...");

            System.setProperty("hazelcast.local.localAddress", "127.0.0.1");

            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        }
        config.getMapConfigs().put("default", initializeDefaultMapConfig(jHipsterProperties));

        // Full reference is available at: http://docs.hazelcast.org/docs/management-center/3.9/manual/html/Deploying_and_Starting.html
        ManagementCenterConfig managementCenterConfig = initializeDefaultManagementCenterConfig(jHipsterProperties);
        config.setManagementCenterConfig(managementCenterConfig);

        return Hazelcast.newHazelcastInstance(config);
    }

    private ManagementCenterConfig initializeDefaultManagementCenterConfig(JHipsterProperties jHipsterProperties) {
        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setEnabled(jHipsterProperties.getCache().getHazelcast().getManagementCenter().isEnabled());
        managementCenterConfig.setUrl(jHipsterProperties.getCache().getHazelcast().getManagementCenter().getUrl());
        managementCenterConfig.setUpdateInterval(jHipsterProperties.getCache().getHazelcast().getManagementCenter().getUpdateInterval());
        return managementCenterConfig;
    }

    private MapConfig initializeDefaultMapConfig(JHipsterProperties jHipsterProperties) {
        MapConfig mapConfig = new MapConfig();

        /*
        Number of backups. If 1 is set as the backup-count for example,
        then all entries of the map will be copied to another JVM for
        fail-safety. Valid numbers are 0 (no backup), 1, 2, 3.
        */
        mapConfig.setBackupCount(jHipsterProperties.getCache().getHazelcast().getBackupCount());

        /*
        Valid values are:
        NONE (no eviction),
        LRU (Least Recently Used),
        LFU (Least Frequently Used).
        NONE is the default.
        */
        mapConfig.setEvictionPolicy(EvictionPolicy.LRU);

        /*
        Maximum size of the map. When max size is reached,
        map is evicted based on the policy defined.
        Any integer between 0 and Integer.MAX_VALUE. 0 means
        Integer.MAX_VALUE. Default is 0.
        */
        mapConfig.setMaxSizeConfig(new MaxSizeConfig(0, MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE));

        return mapConfig;
    }

    private MapConfig initializeDomainMapConfig(JHipsterProperties jHipsterProperties) {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setTimeToLiveSeconds(jHipsterProperties.getCache().getHazelcast().getTimeToLiveSeconds());
        return mapConfig;
    }
}
