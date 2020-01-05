package by.mrj.it.client.longpolling;

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
        return 2;
    }

    @Override
    protected int batchSize() {
        return 10;
    }

    @Override
    protected int timeout() {
        return 1000;
    }

    @Override
    protected ConnectionType connectionType() {
        return ConnectionType.HTTP_LP;
    }

    @Override
    protected ConnectionInfo getConnectionInfo() {
        return ConnectionInfo.from(connectionType(), null, getHost(), getPort(), "login-POST-LP", "pwd");
    }
}
