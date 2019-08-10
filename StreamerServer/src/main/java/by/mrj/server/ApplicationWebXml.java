package by.mrj.server;

import by.mrj.StreamerServerApp;
import by.mrj.server.config.DefaultProfileUtil;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * This is a helper Java class that provides an alternative to creating a {@code web.xml}.
 * This will be invoked only when the application is deployed to a Servlet container like Tomcat, JBoss etc.
 */
public class ApplicationWebXml
//        extends SpringBootServletInitializer
{

//    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        /**
         * set a default to use when no profile is configured.
         */
        DefaultProfileUtil.addDefaultProfile(application.application());
        return application.sources(StreamerServerApp.class);
    }
}
