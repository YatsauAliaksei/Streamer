package by.mrj.dt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class Order {

    private String orderId;
    private List<Allocation> allocations;
    @Setter
    private int totalLots;
}
