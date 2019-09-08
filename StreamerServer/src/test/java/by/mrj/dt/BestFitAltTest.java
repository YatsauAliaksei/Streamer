package by.mrj.dt;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BestFitAltTest {

    @Test
    public void calculate_general() {
        List<Fill> fills = generateFills(30);
        System.out.println("Fills generated.\n" + fills);

        List<Order> orders = generateOrders(4, fills);
        System.out.println("Orders generated.\n" + orders);

//        : {0=1, 1=2, 2=6, 3=1}
//        orders.get(0).setTotalLots(8);
//        orders.get(1).setTotalLots(2);
//        orders.get(2).setTotalLots(4);
//        orders.get(3).setTotalLots(1);

        BestFitAlgDFS bestFitAlgDfs = new BestFitAlgDFS(fills, orders);
        bestFitAlgDfs.calculate();

//        log.info("===============================");
//        log.info("===============================");

//        BestFitAlg bestFitAlg = new BestFitAlg(fills, orders);
//        bestFitAlg.calculate();
    }

    private List<Order> generateOrders(int number, List<Fill> fills) {
        assertThat(number).isLessThan(fills.size());

        List<Order> orders = IntStream.range(0, number)
                .boxed()
                .map(i -> new Order("" + i, null, 1))
                .collect(Collectors.toList());

        int totalLots = fills.stream().mapToInt(Fill::getLots).sum() - number;

        Random random = new Random();
        while (totalLots > 0) {
            int lots = totalLots == 1 ? totalLots : random.nextInt(totalLots);
            Order order = orders.get(random.nextInt(orders.size()));
            order.setTotalLots(order.getTotalLots() + lots);
            totalLots -= lots;
        }

        return orders;
    }

    private List<Fill> generateFills(int number) {
        Random random = new Random();

        return IntStream.range(0, number)
                .boxed()
                .map(n -> new Fill(n, BigDecimal.valueOf(100 + (random.nextDouble() * (random.nextBoolean() ? 1 : -1))), 1, null))
                .collect(toList());
    }
}
