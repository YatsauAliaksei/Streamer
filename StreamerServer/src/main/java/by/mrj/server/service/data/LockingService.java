package by.mrj.server.service.data;

import com.hazelcast.core.ILock;
import lombok.SneakyThrows;

public interface LockingService {

    boolean tryLock(String lockName);

    @SneakyThrows
    boolean tryLock(String lockName, long wait);

    ILock getLock(String lockName);

    void unlock(String lockName);
}
