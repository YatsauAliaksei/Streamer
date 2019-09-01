package by.mrj.dt;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
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

//        leaves.forEach(leaf -> {
//            log.info("Winner. Fill: {}, Order: {}, Price: {}",
//                    leaf.fill.getFillId(),
//                    leaf.order.getOrderId(), leaf.fill.getPrice());
//        });

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

        List<Leaf> minVList = new ArrayList<>();
        BigDecimal minVariance = null;

        int maxCalculations = 1_000_000;
        int totalTerminalWays = 0;
        LinkedList<String> l = new LinkedList<>();
        ListIterator<String> li = l.listIterator();

        Random random = new Random();

//        fills.sort(Comparator.comparing(Fill::getPrice));

        BigDecimal minPrice = fills.get(0).getPrice();
        BigDecimal maxPrice = fills.get(fills.size() - 1).getPrice();
//        fills.sort((o1, o2) -> o1.getPrice().compareTo(o2.getPrice()) * -1);
        totally:
        for (int i = 0; i < orders.size(); i++) {
            Map<String, Integer> orderIdToLotsCopy = new HashMap<>(orderToLots);
            Map<Integer, Integer> fillToOrderIndex = new HashMap<>(fills.size());
//            Map<Integer, Integer> memmory = new HashMap<>(fills.size());

            int fillIndex = 0;

            Fill f = fills.get(fillIndex++);
            stackIn.add(createLeaf(orderIdToLotsCopy, orders.get(i), f));
            fillToOrderIndex.put(f.getFillId(), i);

            while (!stackIn.isEmpty()) {

                Fill fill = fills.get(fillIndex);
                int fillId = fill.getFillId();

                Integer ordLocalIndex = fillToOrderIndex.get(fillId);
                if (ordLocalIndex == null) {
                    ordLocalIndex = -1;
                }

                Leaf nextLeaf = null;
                while (++ordLocalIndex < orders.size()) {
                    fillToOrderIndex.put(fillId, ordLocalIndex);
//                    memmory.put(fillId, ordLocalIndex);

                    Order order = orders.get(ordLocalIndex);

                    nextLeaf = createLeaf(orderIdToLotsCopy, order, fill);
                    if (nextLeaf != null) { // going down
                        stackIn.add(nextLeaf);
                        fillIndex++;
                        break;
                    }
                }

                if (nextLeaf == null) { // horizontal move finished
                    oneStepUp(stackIn, orderIdToLotsCopy);
                    fillToOrderIndex.remove(fillId);
                    fillIndex--;

                    continue;
                }

                if (stackIn.size() == fills.size()) { // terminal node found...
                    totalTerminalWays++;

                    if (!orderIdToLotsCopy.isEmpty())
                        throw new IllegalStateException("Should be empty");

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

                    if (--maxCalculations == 0) {
                        log.info("Calculations limit reached. Stopping..");
                        break totally;
                    }

                    if (minVariance.compareTo(BigDecimal.valueOf(1e-8)) < 0) {
                        log.info("Finishing cause very low variance. {}", variance.toPlainString());
                        log.info("Total number of iterations {}", variance.toPlainString());
                        break totally;
                    }

/*                    orders.sort((o1, o2) -> {
                        String orderId1 = o1.getOrderId();
                        String orderId2 = o2.getOrderId();

                        BigDecimal mes1 = orderToAvg.get(orderId1).subtract(avgFillPrice);
                        BigDecimal mes2 = orderToAvg.get(orderId2).subtract(avgFillPrice);

                        return mes1.compareTo(mes2);

                    });*/

//                    int p = punishment(minVariance.doubleValue());
//                    p = p > stackIn.size() ?
//                    log.info("Punishing level [{}]", p);
//                    for (int s = 0; s < p; s++) {
                    Fill movedUpFill = oneStepUp(stackIn, orderIdToLotsCopy);
//                        fillToOrderIndex.remove(movedUpFill.getFillId());

                    fillIndex--;
//                    }
                }
            }
        }

        log.info("Total terminal ways found [{}]", totalTerminalWays);
        log.info("Winner variance: {}", minVariance.toPlainString());

        return minVList;
    }

    private int punishment(double k) {
        return (int) (fills.size() * k);
    }

    private Fill oneStepUp(Stack<Leaf> stackIn, Map<String, Integer> orderIdToLotsCopy) {
        Leaf lUp = stackIn.pop();
        orderIdToLotsCopy.putIfAbsent(lUp.order.getOrderId(), 0);
        orderIdToLotsCopy.compute(lUp.order.getOrderId(), (k, v) -> ++v);
        return lUp.fill;
    }

    private Leaf createLeaf(Map<String, Integer> map, Order order, Fill fill) {
        String key = order.getOrderId();
        Integer v = map.get(key);
        if (v != null) {
            if (--v > 0) {
                map.put(key, v);
            } else
                map.remove(key);
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
    private class Leaf {
        Order order;
        Fill fill;
    }
}
