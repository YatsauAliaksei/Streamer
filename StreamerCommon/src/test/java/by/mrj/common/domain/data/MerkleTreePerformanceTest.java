package by.mrj.common.domain.data;

import by.mrj.common.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Slf4j
public class MerkleTreePerformanceTest {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        public static MerkleTree merkleTree;
        private static AtomicLong counter = new AtomicLong();

        @Setup(Level.Trial)
        public void before() {
            Map<Long, String> nodes = createStubNodes((1 << 16) / 2);
//            log.info("Created {} nodes", nodes.size());
            merkleTree = new MerkleTree(nodes, 1 << 16);
//            log.info("Tree created");
        }

        public long getNext() {
            long l = counter.incrementAndGet();
            if (l > 60_000) {
                counter.set(0);
                return 1;
            }
            return l;
        }

        public Map<Long, String> getBatch() {
            return LongStream.rangeClosed(0, 1_000)
                    .boxed()
                    .collect(Collectors.toMap(k -> getNext(), String::valueOf, (s, s2) -> s));
        }
    }

    /**
     * 0.016 ms/op for 16 hash 256 invocations.
     * 16 invocations needed for 17 level binary tree which is 2^16 ~ 65.5k final nodes
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void basic(Blackhole blackhole) {
        String hash = "";

        for (int i = 0; i < 16; i++) {
            hash = CryptoUtils.sha256(hash);
        }

        blackhole.consume(hash);
    }

    // ~200 ms/op
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void merkleTreeCreatedBenchmark(Blackhole blackhole) {
        int maxSize = 1 << 16;
        Map<Long, String> stubNodes = createStubNodes(maxSize / 2);
        MerkleTree mt = new MerkleTree(stubNodes, maxSize);

        blackhole.consume(mt);
    }

    // ~0.100 us/op
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void merkleTreeAddBenchmark(BenchmarkState state) {
        long id = state.getNext();
        state.merkleTree.set(id, "hash" + id);
    }

    // ~1.500 ms/op - batch size 1000
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void merkleTreeAddAllBenchmark(BenchmarkState state) {
        Map<Long, String> batch = state.getBatch();
        state.merkleTree.addAll(batch);
    }

    public static Map<Long, String> createStubNodes(int num) {
        Set<BaseObject> nodes = createNodes(num);

        return nodes.stream()
                .collect(Collectors.toMap(BaseObject::getId, BaseObject::getHash));
    }

    public static Set<BaseObject> createNodes(int num) {
        return IntStream.range(0, num)
                .boxed()
                .map(i -> BaseObject.builder()
                        .id((long) i)
                        .payload(String.valueOf(i))
                        .build())
                .collect(Collectors.toSet());
    }
}