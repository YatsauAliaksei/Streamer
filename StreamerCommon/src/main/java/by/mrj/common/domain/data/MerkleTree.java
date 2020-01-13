package by.mrj.common.domain.data;

import by.mrj.common.utils.CryptoUtils;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Merkle tree implementation.
 */
@Slf4j
public class MerkleTree implements Serializable {

    private Node root;
    private Map<Integer, Node> idToNodes;
    /**
     * Doesn't include final nodes
     */
    private Map<Integer, List<Node>> lvlToNodes;

    /**
     * @param idToHash - initial values.
     * @param maxSize  - power of 2
     */
    // todo: validate hash before. Consider TIGER/RIPEMD or any other
    public MerkleTree(Map<Integer, String> idToHash, int maxSize) {
        // todo: maybe just increase maxSize to nearest power of 2
        Preconditions.checkArgument(isPowerOfTwo(maxSize), "Max size should be power of 2");

        lvlToNodes = new HashMap<>((int) log2(maxSize));

        List<Node> nodes = new ArrayList<>(maxSize);
        for (int i = 0; i < maxSize; i++) {
            // todo: think about UUID generation. We need something unique but randomUUID might be to slow
            String hash = idToHash.getOrDefault(i, UUID.randomUUID().toString()).substring(0, 8);
            nodes.add(Node.of(i, hash));
        }

        nodes.sort(Comparator.comparing(o -> o.id));

        idToNodes = nodes.stream()
                .collect(toMap(Node::id, Function.identity()));

        this.root = createTree(nodes);
    }

    public MerkleTree(int maxSize) {
        this(new HashMap<>(), maxSize);
    }

    /**
     * Sets new hash values to node
     * @param id
     * @param hash
     * @return
     */
    public boolean set(Integer id, String hash) {
        Node node = setNode(id, hash);

        if (node == null)
            return false;

        recalculate(node);
        return true;
    }

    private Node setNode(Integer id, String hash) {
        Node node = idToNodes.get(id);
        if (node == null) { // maxSize < id
            log.info("About to recreate for id: {}, hash: {}", id, hash);
            recreate(id, hash);
            return null;
        }

        if (node.hash.equals(hash)) {
            return null;
        }

        node.hash = hash;
        return node;
    }

    public void setAll(Map<Integer, String> idToHash) {
        // todo: recreate operation not handled here
        List<Node> nodes = idToHash.entrySet().stream()
                .map(e -> setNode(e.getKey(), e.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        recalculate(nodes);
    }

    /**
     * @param node - suppose to be final node
     */
    private void recalculate(Node node) {
        while (node.parent != null) {
            node.parent.hash = getCombinedHash(node.parent.left, node.parent.right);
            node = node.parent;
        }
    }

    private void recalculate(Collection<Node> nodes) {
//        nodes.forEach(this::recalculate);

        Set<Node> parents = new HashSet<>();
        do {
            parents.clear();

            for (Node node : nodes) {
                if (node.parent != null) {
                    parents.add((node.parent));
                }
            }

            for (Node parent : parents) {
                parent.hash = getCombinedHash(parent.left, parent.right);
            }
            nodes = new ArrayList<>(parents);
        } while (!parents.isEmpty());
    }

    private Node createTree(List<Node> nodes) {
        List<Node> rootLvl = createLvl(nodes);
        return rootLvl.get(0);
    }

    private List<Node> createLvl(List<Node> nodes) {
        if (nodes.size() == 1)
            return nodes;

        List<Node> level = new ArrayList<>();

        for (int i = 0; i < nodes.size(); i += 2) {
            Node odd = nodes.get(i);
            Node even = nodes.get(i + 1);

            Node parent = createParent(odd, even);

            level.add(parent);
        }

        int lvlIndex = calculateLvlIndex(level);
        lvlToNodes.put(lvlIndex, level);

        return createLvl(level);
    }

    public List<Node> getLevel(int index) {
        return lvlToNodes.getOrDefault(index, new ArrayList<>());
    }

    private int calculateLvlIndex(List<Node> level) {
        double i = log2(level.size());
        // todo: check
        return (int) i;
    }

    private Node createParent(Node odd, Node even) {
        String combinedHash = getCombinedHash(odd, even);

        Node parent = Node.of(null, combinedHash);
        odd.parent = parent;
        even.parent = parent;
        parent.left = odd;
        parent.right = even;
        return parent;
    }

    /**
     * Adding new level in case not enough space
     * Generally means full tree recreation
     */
    private void recreate(Integer id, String hash) {
        // todo:
        throw new UnsupportedOperationException("Recreate invocation");
    }

    private String getCombinedHash(Node odd, Node even) {
        return CryptoUtils.sha256(odd.hash + even.hash).substring(0, 8);
    }

    private Node getSibling(Node node) {
        if (node.parent.left == node) {
            return node.parent.right;
        } else if (node.parent.right == node) {
            return node.parent.left;
        } else
            throw new IllegalStateException();
    }

    public String rootHash() {
        return root.hash;
    }

    public List<Node> parentsOf(int id) {
        Node node = idToNodes.get(id);
        if (node == null) {
            return Collections.emptyList();
        }

        List<Node> parents = new ArrayList<>(levelSize());
        while (node.parent != null) {
            parents.add(node.parent);
            node = node.parent;
        }

        return parents;
    }

    /**
     * @return - number of final nodes
     */
    public int size() {
        return idToNodes.size();
    }

    public int levelSize() {
        return lvlToNodes.size();
    }

    public List<Node> level(int level) {
        return lvlToNodes.get(level);
    }

    public Node getNode(int level, int index) {
        List<Node> nodes = lvlToNodes.getOrDefault(level, new ArrayList<>());
        return index < nodes.size() ? nodes.get(index) : null;
    }

    private boolean isPowerOfTwo(int num) {
        return (num != 0) && ((num & (num - 1)) == 0);
    }

    private double log2(int x) {
        return Math.log(x) / Math.log(2);
    }

    @ToString(exclude = {"parent", "left", "right"})
    @EqualsAndHashCode(of = {"id", "hash"})
    @RequiredArgsConstructor
    public static class Node implements Serializable {

        public Node(Integer id, String hash) {
            this.id = id;
            this.hash = hash;
        }

        private Node parent;
        private Node left;
        private Node right;
        private final Integer id;
        private String hash;

        public Integer id() {
            return id;
        }

        public String hash() {
            return hash;
        }

        private static Node of(Integer id, String hash) {
            return new Node(id, hash);
        }
    }
}
