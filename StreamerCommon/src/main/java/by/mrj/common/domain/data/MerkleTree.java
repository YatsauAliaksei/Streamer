package by.mrj.common.domain.data;

import by.mrj.common.utils.CryptoUtils;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class MerkleTree {

    private final Node root;
    private final Map<String, Node> assetNodes;

    public MerkleTree(Set<Hashable> nodes) {
        List<Node> nodesHashes = nodes.stream()
                .map(Hashable::hash)
                .map(Node::new)
                .collect(toList());

        this.assetNodes = nodesHashes.stream()
                .collect(toMap(Node::hash, Function.identity(), (v1, v2) -> {
                    throw new IllegalStateException();
                }, LinkedHashMap::new));

        this.root = createTree(nodesHashes);
    }

    private Node createTree(List<Node> hashes) {
        List<Node> rootLvl = createLvl(hashes);
        return rootLvl.get(0);
    }

    private List<Node> createLvl(List<Node> nodes) {
        if (nodes.size() == 1)
            return nodes;

        List<Node> level = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i += 2) {
            Node odd = nodes.get(i);
            Node even = i + 1 < nodes.size() ? nodes.get(i + 1) : odd;

            Node parent = createParent(odd, even);

            level.add(parent);
        }
        return createLvl(level);
    }

    private Node createParent(Node odd, Node even) {
        String combinedHash = CryptoUtils.doubleSha256(odd.hash + even.hash);

        Node parent = Node.of(combinedHash);
        odd.parent = parent;
        even.parent = parent;
        parent.left = odd;
        parent.right = even;
        return parent;
    }

    public List<String> proof(String assetHash) {
        Node node = assetNodes.get(assetHash);

        if (node == null)
            return Collections.emptyList();

        List<String> hashesToProof = new ArrayList<>();

        while (node.parent != null) {
            Node sibling = getSibling(node);
            hashesToProof.add(sibling.hash);
            node = sibling.parent;
        }

        return hashesToProof;
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

    @RequiredArgsConstructor(staticName = "of")
    private static class Node {
        Node parent;
        Node left;
        Node right;
        final String hash;

        String hash() {
            return hash;
        }
    }
}
