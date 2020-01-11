package by.mrj.server.service.data;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class HzLockingService implements LockingService {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public boolean tryLock(String lockName) {
        return hazelcastInstance.getLock(lockName).tryLock();
    }

    @Override
    @SneakyThrows
    public boolean tryLock(String lockName, long wait) {
        return hazelcastInstance.getLock(lockName).tryLock(wait, TimeUnit.MILLISECONDS);
    }

    @Override
    public ILock getLock(String lockName) {
        return hazelcastInstance.getLock(lockName);
    }

    @Override
    public void unlock(String lockName) {
        hazelcastInstance.getLock(lockName).forceUnlock();
    }
}
