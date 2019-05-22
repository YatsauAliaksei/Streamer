package by.mrj.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@EqualsAndHashCode
//@NoArgsConstructor
@Builder
public class Message<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;
//    @NonNull
    private
    String checksum;
//    @NonNull
    private
    String address;
//    @NonNull
    private
    T payload;
//    @NonNull
    private
    String publicKey;
    @Setter
    private
    String signature;
}
