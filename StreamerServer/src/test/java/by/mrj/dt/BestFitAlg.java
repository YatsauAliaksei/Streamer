package by.mrj.dt;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toMap;

@Slf4j
@RequiredArgsConstructor
public class BestFitAlg {

    private final List<Fill> fills;
    private final List<Order> orders;

    public void calculate() {

        long start = System.nanoTime();

        DecisionTree decisionTree = createDecisionTree();

        log.info("Termination nodes calculated: " + decisionTree.terminalNodes.size());

//        check(decisionTree);

        log.debug("Avg fills price calculation");

        BigDecimal avgFillPrice = fills.stream()
                .map(Fill::getPrice)
                .reduce(BigDecimal::add).get().divide(BigDecimal.valueOf(fills.size()), 6, RoundingMode.HALF_UP);

        log.debug("Variance calculation...");
        Node winner = null;
        BigDecimal min = null;
        for (Node node : decisionTree.terminalNodes) {
            BigDecimal variance = calculateVariance(node, avgFillPrice);
//            log.debug("Fill: {}, Order: {}, Price: {}, Variance: {}", node.fillId, node.orderId, node.price, variance);

            if (min == null) {
                min = variance;
                winner = node;
            } else if (variance.compareTo(min) < 1) {
                min = variance;
                winner = node;
            }
        }

        log.debug("========== WINNER =========");

        Map<String, BigDecimal> orderToAvgPrice = orderToAvgPrice(winner);
        orderToAvgPrice.forEach((id, avg) -> log.info("Order: [{}] - {}", id, avg));

        while (winner != null) {
            log.info("Winner. Fill: {}, Order: {}, Price: {}", winner.fillId, winner.orderId, winner.price);
            winner = winner.parent;
        }
        log.info("Final variance: {}", min);

        log.info("Time: {}", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    }

    private void check(DecisionTree decisionTree) {
        log.debug("Checking...");

        long count = decisionTree.terminalNodes.stream()
                .map(node -> {
                    Node p = node;
                    BigDecimal sum = BigDecimal.ZERO;
                    while (p != null) {
                        log.debug("Fill: {}, Order: {}, Price: {}", p.fillId, p.orderId, p.price);
                        sum = sum.add(p.price);
                        p = p.parent;
                    }
                    return sum;
                }).distinct().count();
        if (count != 1) {
            throw new IllegalStateException("Different sum.");
        }
    }

    private DecisionTree createDecisionTree() {
        log.debug("Creating DT...");

        Map<String, Integer> orderToLots = orders.stream()
                .collect(toMap(Order::getOrderId, Order::getTotalLots));

        DecisionTree decisionTree = new DecisionTree();
        decisionTree.root = createRootNode();
        decisionTree.root.orderIdToLots = orderToLots;

        Queue<List<Node>> queue = new ArrayDeque<>();
        queue.add(Lists.newArrayList(decisionTree.root));

        log.info("Order to Lots: " + orderToLots);

        long nodesCreated = 0;
        for (int k = 0; k < fills.size(); k++) {
            List<Node> newParents = new ArrayList<>();
            while (!queue.isEmpty()) {
                List<Node> parents = queue.poll();

                for (Node parent : parents) {
                    for (int i = 0; i < orders.size(); i++) {
                        Node subNode = createNode(parent.orderIdToLots, orders.get(i).getOrderId(), fills.get(k));

                        if (subNode == null) {
                            continue;
                        }

                        nodesCreated++;

                        parent.subNodes[i] = subNode;
                        if (parent != decisionTree.root) {
                            subNode.parent = parent;
                        }
//                        System.out.println("Node created. Level: " + k + "   Order: " + i + "  Price: " + subNode.price);

                        if (subNode.orderIdToLots.isEmpty()) {
                            decisionTree.terminalNodes.add(subNode);
                        } else {
                            newParents.add(subNode);
                        }
                    }
                }
            }

            log.debug("Nodes created: [{}]", nodesCreated);

            queue.add(newParents);
        }

        return decisionTree;
    }

    private BigDecimal calculateVariance(Node node, BigDecimal fillsAverage) {
        Map<String, BigDecimal> orderToAvg = orderToAvgPrice(node);

        return orderToAvg.values().stream()
                .map(orderAvg -> orderAvg.subtract(fillsAverage).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(orderToAvg.size()), 10, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> orderToAvgPrice(Node node) {
        Map<String, List<Node>> fills = new HashMap<>();

        while (node != null && node.orderId != null /* fixme: root node */) {
            List<Node> nodes = fills.get(node.orderId);
            if (nodes == null) {
                nodes = new ArrayList<>();
                fills.put(node.orderId, nodes);
            }

            nodes.add(node);
            node = node.parent;
        }

        Map<String, BigDecimal> orderToAvg = new HashMap<>();

        fills.forEach((orderId, nodes) -> {
            BigDecimal orderAvg = nodes.stream()
                    .map(n -> n.price)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(nodes.size()), 6, RoundingMode.HALF_UP);

            orderToAvg.put(orderId, orderAvg);
        });
        return orderToAvg;
    }

    private Node createRootNode() {
        Node node = new Node();
        node.price = BigDecimal.ZERO;
        return node;
    }

    private Node createNode(Map<String, Integer> orderToLots, String orderId, Fill fill) {
        Integer totalLots = orderToLots.get(orderId);
        if (totalLots == null) {
//            return stubNode;
            return null;
        }

        Node node = new Node();
        node.price = fill.getPrice();
        node.orderId = orderId;
        node.orderIdToLots = new HashMap<>(orderToLots);
        node.fillId = fill.getFillId();

        if (--totalLots > 0) {
            node.orderIdToLots.put(orderId, totalLots);
        } else {
            node.orderIdToLots.remove(orderId);
        }

        return node;
    }

    private class DecisionTree {
        Node root;
        List<Node> terminalNodes = new ArrayList<>();
    }

    private class Node {
        Node parent;
        Node[] subNodes = new Node[orders.size()];
        Map<String, Integer> orderIdToLots;

        int fillId = -1;
        String orderId;
        BigDecimal price;
    }
}
