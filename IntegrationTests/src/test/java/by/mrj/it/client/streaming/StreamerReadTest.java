package by.mrj.it.client.streaming;

import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.it.client.AbstractStreamerReadTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@SpringBootTest
        (classes = {
                StreamerClientConfiguration.class,
                StreamerReadTest.class
        }, properties = {
                "config/application-dev.yml", "spring.main.banner-mode=off"
        })
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
public class StreamerReadTest extends AbstractStreamerReadTest {

    @Override
    protected List<ConnectionInfo> connectionInfo() {
        return IntStream.range(0, 2)
                .boxed()
                .map(i -> createConnectionInfo("login-READ-ST-" + i))
                .collect(Collectors.toList());
    }

    private ConnectionInfo createConnectionInfo(String login) {
        return ConnectionInfo.from(ConnectionType.HTTP_STREAMING, null, getHost(), getPort(), login, "pwd");
    }
}
