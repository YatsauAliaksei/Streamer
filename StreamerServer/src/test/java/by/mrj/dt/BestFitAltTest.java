package by.mrj.dt;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class BestFitAltTest {

    @Test
    public void calculate_general() {
        List<Fill> fills = generateFills(15);
        System.out.println("Fills generated.\n" + fills);

        List<Order> orders = generateOrders(3, fills);
        System.out.println("Orders generated.\n" + orders);

        BestFitAlg bestFitAlg = new BestFitAlg(fills, orders);
        bestFitAlg.calculate();
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
