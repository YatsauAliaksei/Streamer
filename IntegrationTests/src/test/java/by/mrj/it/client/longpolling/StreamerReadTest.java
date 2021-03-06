package by.mrj.it.client.longpolling;

import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.it.client.AbstractStreamerReadTest;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.common.value.qual.IntRange;
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
        return IntStream.rangeClosed(1, 3)
                .boxed()
                .map(i -> createConnectionInfo("login-READ-LP-" + i))
                .collect(Collectors.toList());
    }

    private ConnectionInfo createConnectionInfo(String login) {
        return ConnectionInfo.from(ConnectionType.HTTP_LP, null, getHost(), getPort(), login, "pwd");
    }
}
