package by.mrj.server.service.merkletree;

import by.mrj.common.domain.data.MerkleTree;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
public class MerkleTreeService {

    private final int initSize;

    private ConcurrentMap<String, MerkleTree> topicToMerkleTree = new ConcurrentHashMap<>();

    public MerkleTree get(String topicName) {
        return topicToMerkleTree.get(topicName);
    }

    public MerkleTree createFor(String topicName) {
        return topicToMerkleTree.computeIfAbsent(topicName, t -> new MerkleTree(initSize));
    }

    public void set(String topicName, Long id, String hash) {
        MerkleTree merkleTree = topicToMerkleTree.get(topicName);
        merkleTree.set(id, hash);
    }

    public void setAll(String topicName, Map<Long, String> idToHash) {
        MerkleTree merkleTree = topicToMerkleTree.get(topicName);
        merkleTree.setAll(idToHash);
    }
}
