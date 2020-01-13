package by.mrj.common.domain.data;

import com.hazelcast.wan.merkletree.ArrayMerkleTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 5, jvmArgsAppend = {"-Xms512g", "-Xmx512g"})
@State(Scope.Benchmark)
public class HzMTPerformanceTest {
    private static final int HUGE_PRIME = 982455757;
    private static final int PREFILL_COUNT = 100000;

    private int anInt = HUGE_PRIME;

    @Benchmark
    @Fork(value = 5, jvmArgsAppend = {"-Xms4g", "-Xmx4g"})
    public void updateAdd_heap_4G(BenchmarkContext context) {
        for (int i = 0; i < 1000; i++) {
            int anEntry = getAnInt();
            context.merkleTree.updateAdd(anEntry, anEntry);
        }
    }

    @Benchmark
    @Fork(value = 5, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
    public void updateAdd_heap_2G(BenchmarkContext context) {
        int anEntry = getAnInt();
        context.merkleTree.updateAdd(anEntry, anEntry);
    }

    @Benchmark
    @Fork(value = 5, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
    public void updateAdd_heap_1G(BenchmarkContext context) {
        int anEntry = getAnInt();
        context.merkleTree.updateAdd(anEntry, anEntry);
    }

    @Benchmark
    public void updateReplace(PreFilledBenchmarkContext context) {
        int key = getAnInt(PREFILL_COUNT);
        int oldValue = key;
        int newValue = getAnInt();
        context.merkleTree.updateReplace(key, oldValue, newValue);
    }

    @Benchmark
    public void updateRemove(PreFilledBenchmarkContext context) {
        int key = getAnInt(PREFILL_COUNT);
        int value = key;
        context.merkleTree.updateRemove(key, value);
    }

    private int getAnInt() {
        anInt += HUGE_PRIME;
        return anInt;
    }

    private int getAnInt(int max) {
        anInt = (anInt + HUGE_PRIME) % max;
        return anInt;
    }

    @State(Scope.Benchmark)
    public static class BenchmarkContext {
        @Param({ /*"8", "9", "10", "11", "12", "13", "14", "15", */ "16" /*, "17", "18", "19", "20" */})
        protected int depth;

        protected com.hazelcast.wan.merkletree.MerkleTree merkleTree;

        @Setup(Level.Trial)
        public void setUp() {
            merkleTree = new ArrayMerkleTree(depth);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            System.out.println("Depth: " + depth);
            System.out.println("Footprint: " + merkleTree.footprint() / 1024 + " (KB)");
        }
    }

    @State(Scope.Benchmark)
    public static class PreFilledBenchmarkContext extends BenchmarkContext {

        @Setup(Level.Trial)
        public void setUp() {
            super.setUp();
            for (int i = 0; i < PREFILL_COUNT; i++) {
                int value = i * HUGE_PRIME;
                merkleTree.updateAdd(value, value);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HzMTPerformanceTest.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                //                .addProfiler(SafepointsProfiler.class)
//                                .addProfiler(LinuxPerfProfiler.class)
                .addProfiler(GCProfiler.class)
                //                .addProfiler(HotspotMemoryProfiler.class)
                //                .verbosity(VerboseMode.SILENT)
                .build();

        new Runner(opt).run();
    }
}
