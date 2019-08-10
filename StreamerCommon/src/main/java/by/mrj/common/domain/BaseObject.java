package by.mrj.common.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode
//@NoArgsConstructor
@Builder
public class BaseObject implements Serializable {
    private int _1st;
//    private int _2nd;
//    private int _3rd;
//    private int _4th;
//    private int _5th;
//    private String text;
}
