package by.mrj;

import by.mrj.client.config.ApplicationProperties;
import by.mrj.client.config.DefaultProfileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class StreamerClientApp implements InitializingBean {

    private final Environment env;

    public StreamerClientApp(Environment env) {
        this.env = env;
        log.info("Active profiles [{}]", this.env.getActiveProfiles());
    }

    /**
     * Main method, used to run the application.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        log.info("Starting client...");
        SpringApplication app = new SpringApplication(StreamerClientApp.class);
        app.setBannerMode(Banner.Mode.OFF);
        DefaultProfileUtil.addDefaultProfile(app);
        app.run(args);
//        Environment env = app.run(args).getEnvironment();
//        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port");
        String streamerPort = env.getProperty("streamer.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}{}\n\t" +
                        "Listen port: \t{}\n\t" +
                        "External: \t{}://{}:{}{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                streamerPort,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles());
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
