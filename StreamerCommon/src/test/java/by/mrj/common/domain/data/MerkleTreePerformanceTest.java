package by.mrj.common.domain.data;

import by.mrj.common.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Slf4j
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
//@Fork(value = 5, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
public class MerkleTreePerformanceTest {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        public static MerkleTree merkleTree;
        private static AtomicInteger counter = new AtomicInteger();

        @Setup(Level.Trial)
        public void before() {
            Map<Integer, String> nodes = createStubNodes((1 << 16) / 2);
//            log.info("Created {} nodes", nodes.size());
            merkleTree = new MerkleTree(nodes, 1 << 16);
//            log.info("Tree created");
        }

        public int getNext() {
            int l = counter.incrementAndGet();
            if (l > 60_000) {
                counter.set(0);
                return 1;
            }
            return l;
        }

        public Map<Integer, String> getBatch() {
            return IntStream.range(0, 1_000)
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
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
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
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void merkleTreeCreatedBenchmark(Blackhole blackhole) {
        int maxSize = 1 << 16;
        Map<Integer, String> stubNodes = createStubNodes(maxSize / 2);
        MerkleTree mt = new MerkleTree(stubNodes, maxSize);

        blackhole.consume(mt);
    }

    // ~0.100 us/op
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void merkleTreeAddBenchmark(BenchmarkState state) {
        int id = state.getNext();
        state.merkleTree.set(id, "hash" + id);
    }

    // ~1.500 ms/op - batch size 1000
    @Benchmark
    public void merkleTreeAddAllBenchmark(BenchmarkState state) {
        Map<Integer, String> batch = state.getBatch();
        batch.forEach((k, v) -> {
            state.merkleTree.set(k, v);
        });
//        state.merkleTree.setAll(batch);
    }

    public static Map<Integer, String> createStubNodes(int num) {
        Set<BaseObject> nodes = createNodes(num);

        return nodes.stream()
                .collect(Collectors.toMap(BaseObject::getId, BaseObject::getHash));
    }

    public static Set<BaseObject> createNodes(int num) {
        return IntStream.range(0, num)
                .boxed()
                .map(i -> BaseObject.builder()
                        .id(i)
                        .payload(String.valueOf(i))
                        .build())
                .collect(Collectors.toSet());
    }
}