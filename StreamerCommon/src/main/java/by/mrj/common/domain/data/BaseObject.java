package by.mrj.common.domain.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "payload")
@EqualsAndHashCode(exclude = "payload")
public class BaseObject implements Serializable {

    private String uuid;
    private String topic;
    private int version;

    private String payload;
}
