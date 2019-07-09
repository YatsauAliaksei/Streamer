package by.mrj.config.streamer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiServiceExporter;

//@Slf4j
//@Configuration
//@ConditionalOnProperty("streamer.rmi.enabled")
public class RmiConfiguration {

/*    @Bean
    RmiServiceExporter exporter(CabBookingService implementation) {
        Class<CabBookingService> serviceInterface = CabBookingService.class;
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceInterface(serviceInterface);
        exporter.setService(implementation);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(1099);
        return exporter;
    }*/
}
