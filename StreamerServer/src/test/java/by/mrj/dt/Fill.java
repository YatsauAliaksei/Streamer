package by.mrj.dt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@AllArgsConstructor
@ToString(exclude = {"lots", "timestamp"})
public class Fill {

    private int fillId;
    private BigDecimal price;
    private int lots = 1;
    private Date timestamp;
}
