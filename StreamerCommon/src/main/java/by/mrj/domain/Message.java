package by.mrj.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    String checksum;
    String address;
    T payload;
    String publicKey;
    @Setter
    String signature;
}
