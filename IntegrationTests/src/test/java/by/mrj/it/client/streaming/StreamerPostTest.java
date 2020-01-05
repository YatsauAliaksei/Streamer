package by.mrj.it.client.streaming;

import by.mrj.client.config.streamer.StreamerClientConfiguration;
import by.mrj.common.domain.ConnectionType;
import by.mrj.common.domain.client.ConnectionInfo;
import by.mrj.it.client.AbstractStreamerPostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@Slf4j
@SpringBootTest
        (classes = {
                StreamerClientConfiguration.class,
                StreamerPostTest.class
        }, properties = {
                "config/application-dev.yml", "spring.main.banner-mode=off"
        })
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
public class StreamerPostTest extends AbstractStreamerPostTest {

    @Override
    protected int batches() {
        return 50;
    }

    @Override
    protected int batchSize() {
        return 1;
    }

    @Override
    protected int timeout() {
        return 200;
    }

    @Override
    protected ConnectionType connectionType() {
        return ConnectionType.HTTP_STREAMING;
    }

    @Override
    protected ConnectionInfo getConnectionInfo() {
        return ConnectionInfo.from(ConnectionType.HTTP_STREAMING, null, getHost(), getPort(), "login-POST-ST", "pwd");
    }
}
