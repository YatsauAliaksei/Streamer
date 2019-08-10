package by.mrj.common.domain;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MessageHeader implements Serializable {
    @Getter
    private Command command;
}