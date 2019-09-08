package by.mrj.dt;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toMap;

@Slf4j
@RequiredArgsConstructor
public class BestFitAlgDFS {

    private final List<Fill> fills;
    private final List<Order> orders;

    public void calculate() {

        long start = System.nanoTime();

        List<Leaf> leaves = createDecisionTree();

        log.info("Fills calculated: " + leaves.size());

        log.debug("========== WINNER =========");

        Map<String, BigDecimal> orderToAvgPrice = orderToAvgPrice(leaves);
        orderToAvgPrice.forEach((id, avg) -> log.info("Order: [{}] - {}", id, avg));

        log.info("Time: {}", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    }

    private BigDecimal avgFillPrice() {
        return fills.stream()
                .map(Fill::getPrice)
                .reduce(BigDecimal::add).get().divide(BigDecimal.valueOf(fills.size()), 6, RoundingMode.HALF_UP);
    }

    private List<Leaf> createDecisionTree() {
        log.debug("Creating DT...");

        Map<String, Integer> orderToLots = orders.stream()
                .collect(toMap(Order::getOrderId, Order::getTotalLots));

        log.info("Order to Lots: " + orderToLots);

        Stack<Leaf> stackIn = new Stack<>();
        BigDecimal avgFillPrice = avgFillPrice();

        log.info("Avg price [{}]", avgFillPrice.toPlainString());

        List<Leaf> minVList = new ArrayList<>();
        BigDecimal minVariance = null;

        int maxCalculations = 1_000_000;
        int totalTerminalWays = 0;

        orders.sort(Comparator.comparing(Order::getTotalLots));
        fills.sort(Comparator.comparing(f -> f.getPrice().subtract(avgFillPrice).pow(2)));

        log.info("Orders sorted: [{}]", orders);

        log.info("Min delta from avg: [{}]", fills.get(0).getPrice());
        log.info("Max delta from avg: [{}]", fills.get(fills.size() - 1).getPrice());

        totally:
        for (int i = 0; i < orders.size(); i++) {
            Map<String, Integer> orderIdToLotsCopy = new HashMap<>(orderToLots);
            Map<Integer, Integer> fillToOrderIndex = new HashMap<>(fills.size());
            Map<Integer, Integer> fillToOrderIndexInitial = new HashMap<>(fills.size());

            int fillIndex = 0;

            Fill f = fills.get(fillIndex++);
            Leaf leaf = createLeaf(orderIdToLotsCopy, orders.get(i), f);
            stackIn.add(leaf);
            log.info("Initial. I [{}]. Leaf created [{}]", i, leaf);

            fillToOrderIndex.put(f.getFillId(), i);

            while (!stackIn.isEmpty()) {

                Fill fill = fills.get(fillIndex);
                int fillId = fill.getFillId();

                log.debug("Processing fill. Index [{}], Fill [{}]", fillIndex, fill);

                Integer ordLocalIndex = fillToOrderIndex.get(fillId);
                if (ordLocalIndex == null) {
                    ordLocalIndex = 0;

                    if (fillIndex > 0 && fillIndex < fills.size()) { // excluding final leafs
                        Fill prevFill = fills.get(fillIndex - 1);
                        Integer prevIndex = fillToOrderIndex.get(prevFill.getFillId());

                        log.debug("Previous H index [{}]", prevIndex);

                        ordLocalIndex = nextHIndex(prevIndex);
                    }
                    fillToOrderIndexInitial.put(fillId, ordLocalIndex);
                } else if ((++ordLocalIndex).equals(fillToOrderIndexInitial.get(fillId))) {
                    ordLocalIndex = Integer.MAX_VALUE;
                    log.debug("Skipping FI: [{}]", fillIndex);
                }

                Leaf nextLeaf = null;
                while (ordLocalIndex < orders.size()) {

                    log.debug("Next H index [{}]", ordLocalIndex);
                    fillToOrderIndex.put(fillId, ordLocalIndex);

                    Order order = orders.get(ordLocalIndex);

                    nextLeaf = createLeaf(orderIdToLotsCopy, order, fill);

                    int nextIndex;
                    if (nextLeaf != null) { // going down
                        log.debug("Down. Leaf created [{}]", nextLeaf);
                        stackIn.add(nextLeaf);
                        fillIndex++;
                        break;
                    } else if (fillToOrderIndexInitial.get(fillId).equals(nextIndex = nextHIndex(ordLocalIndex))) {
                        log.debug("H finished. FI: [{}], LI: [{}], N: [{}]", fillIndex, ordLocalIndex, nextIndex);
                        break;
                    }

                    ordLocalIndex = nextIndex;
                }

                if (nextLeaf == null) { // horizontal move finished. Going Up
                    oneStepUp(stackIn, orderIdToLotsCopy);
                    fillToOrderIndex.remove(fillId);
                    fillToOrderIndexInitial.remove(fillId);

                    fillIndex--;

                    log.debug("Up. Fill index [{}]", fillIndex);

                    continue;
                }

                if (stackIn.size() == fills.size()) { // terminal node found...
                    totalTerminalWays++;

                    if (!orderIdToLotsCopy.isEmpty()) {

                        log.info("Orders left: [{}]", orderIdToLotsCopy);
                        throw new IllegalStateException("Should be empty");
                    }

                    Map<String, BigDecimal> orderToAvg = orderToAvgPrice(stackIn);

                    BigDecimal variance = calculateVariance(orderToAvg, avgFillPrice);
                    if (minVariance == null) {
                        minVariance = variance;
                        minVList = new ArrayList<>(stackIn);

                        log.info("V: {}", variance.toPlainString());
                    } else if (minVariance.compareTo(variance) > 0) {
                        minVariance = variance;
                        minVList = new ArrayList<>(stackIn);

                        log.info("V: {}", variance.toPlainString());
                    }
//                    log.info("V: {}", variance.toPlainString());

                    if (--maxCalculations == 0) {
                        log.info("Calculations limit reached. Stopping..");
                        break totally;
                    }

                    if (minVariance.compareTo(BigDecimal.valueOf(1e-8)) < 0) {
                        log.info("Finishing cause very low variance. {}", variance.toPlainString());
                        log.info("Total number of iterations {}", variance.toPlainString());
                        break totally;
                    }

                    oneStepUp(stackIn, orderIdToLotsCopy);
                    fillIndex--;
                }
            }
        }

        log.info("Total terminal ways found [{}]", totalTerminalWays);
        if (minVariance == null) {
            System.out.println("");
        }
        log.info("Winner variance: {}", minVariance.toPlainString());

        return minVList;
    }

    private int nextHIndex(int currentIndex) {
        return currentIndex + 1 < orders.size() ? currentIndex + 1 : 0;
    }

    private Fill oneStepUp(Stack<Leaf> stackIn, Map<String, Integer> orderIdToLotsCopy) {
        Leaf lUp = stackIn.pop();
        orderIdToLotsCopy.putIfAbsent(lUp.order.getOrderId(), 0);
        orderIdToLotsCopy.compute(lUp.order.getOrderId(), (k, v) -> ++v);
        return lUp.fill;
    }

    private Leaf createLeaf(Map<String, Integer> orderIdToLeftLots, Order order, Fill fill) {
        String key = order.getOrderId();
        Integer v = orderIdToLeftLots.get(key);
        if (v != null) {
            if (--v > 0) {
                orderIdToLeftLots.put(key, v);
            } else
                orderIdToLeftLots.remove(key);
        } else
            return null;

        return new Leaf(order, fill);
    }

    private BigDecimal calculateVariance(Map<String, BigDecimal> orderToAvg, BigDecimal fillsAverage) {

        return orderToAvg.values().stream()
                .map(orderAvg -> orderAvg.subtract(fillsAverage).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(orderToAvg.size()), 6, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> orderToAvgPrice(Collection<Leaf> stack) {
        Map<String, List<Leaf>> fills = stack.stream()
                .collect(toMap(k -> k.order.getOrderId(), v -> Lists.newArrayList(v), (o, o2) -> {
                            o.addAll(o2);
                            return o;
                        }
                ));

        Map<String, BigDecimal> orderToAvg = new HashMap<>();

        fills.forEach((orderId, leaves) -> {
            BigDecimal orderAvg = leaves.stream()
                    .map(l -> l.fill.getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(leaves.size()), 6, RoundingMode.HALF_UP);

            orderToAvg.put(orderId, orderAvg);
        });
        return orderToAvg;
    }

    @AllArgsConstructor
    @ToString
    private class Leaf {
        Order order;
        Fill fill;
    }
}
