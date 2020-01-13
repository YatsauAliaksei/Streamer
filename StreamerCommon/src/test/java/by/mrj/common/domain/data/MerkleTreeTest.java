package by.mrj.common.domain.data;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static by.mrj.common.domain.data.MerkleTreePerformanceTest.createStubNodes;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class MerkleTreeTest {

    @Test
    public void merkleTreeCreatedTest() {
        int maxSize = 1 << 16;
        MerkleTree mt = createMerkleTree(maxSize);

        assertThat(mt.size()).isEqualTo(maxSize);

        for (int i = 0; i < 16; i++) {
            List<MerkleTree.Node> level = mt.getLevel(i);
            assertThat(level).hasSize(1 << i);
        }

        MerkleTree.Node node = mt.getNode(0, 0);
        assertThat(node.hash()).isEqualTo(mt.rootHash());
    }

    @Test
    public void merkleTreeAddTest() {
        int levels = 10;
        MerkleTree mt = createMerkleTree(1 << levels);

        int ls = mt.levelSize();
        assertThat(ls).isEqualTo(levels);
        assertThat(mt.size()).isEqualTo((int) Math.pow(2, levels));

        List<String> hashes = hashSnapshot(levels, mt);

        // checks all hashes to be as expected
        for (int i = 0; i < levels; i++) {
            List<MerkleTree.Node> level = mt.getLevel(i);
            for (int n = 0; n < level.size(); n++) {
                assertThat(mt.getNode(i, n).hash()).isEqualTo(hashes.get((int) (Math.pow(2, i) - 1) + n));
            }
        }

        for (int i = 0; i < mt.size(); i++) {
            hashes = hashSnapshot(levels, mt);

            mt.set(i, "hash" + i);
            checkTreeAfterChange(levels, mt, hashes, Lists.newArrayList(i));
        }
    }

    @Test
    public void merkleTreeAddAllTest() {
        int levels = 5;
        MerkleTree mt = createMerkleTree(1 << levels);
        // to guarantee unique of hash on each update
        AtomicInteger incrementalId = new AtomicInteger();

        for (int i = 0; i < mt.size(); i++) {
            List<Integer> randomBatch = getRandomBatch(mt.size() / 4, mt.size());

            Map<Integer, String> idToHash = randomBatch.stream()
                    .collect(Collectors.toMap(Function.identity(), k -> "hash" + incrementalId.incrementAndGet()));

            List<String> hashes = hashSnapshot(levels, mt);

            mt.setAll(idToHash);

            checkTreeAfterChange(levels, mt, hashes, randomBatch);
        }
    }

    private List<Integer> getRandomBatch(int batchSize, int maxId) {
        List<Integer> batchIds = new ArrayList<>(batchSize);
        Random random = new Random();

        while (batchIds.size() < batchSize) {
            int i = random.nextInt(maxId);
            if (batchIds.contains(i) || i == 0) {
                continue;
            }

            batchIds.add(i);
        }

        return batchIds;
    }

    private List<String> hashSnapshot(int levels, MerkleTree mt) {
        List<String> hashes = new ArrayList<>((1 << levels) - 1);
        for (int i = 0; i < mt.levelSize(); i++) {
            List<MerkleTree.Node> level = mt.getLevel(i);
            for (MerkleTree.Node node : level) {
                hashes.add(node.hash());
            }
        }
        return hashes;
    }

    private void checkTreeAfterChange(int levels, MerkleTree mt, List<String> hashes, List<Integer> ids) {
        Set<MerkleTree.Node> parents = ids.stream()
                .flatMap(id -> mt.parentsOf(id).stream())
                .collect(Collectors.toSet());

        for (int i = 0; i < levels; i++) {

            List<MerkleTree.Node> level = mt.getLevel(i);
            for (int n = 0; n < level.size(); n++) {

                MerkleTree.Node node = mt.getNode(i, n);

                String after = node.hash();
                String before = hashes.get((int) (Math.pow(2, i) - 1) + n);

                if (parents.contains(node)) {
                    assertThat(after)
                            .withFailMessage("Level: %d  idx: %d", i, n)
                            .isNotEqualTo(before);
                } else {
                    assertThat(after).isEqualTo(before);
                }

            }
        }
    }

    private MerkleTree createMerkleTree(int maxSize) {
        Map<Integer, String> stubNodes = createStubNodes(maxSize / 2);
        return new MerkleTree(stubNodes, maxSize);
    }
}
