package by.mrj.common.domain.data;

import by.mrj.common.utils.CryptoUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "payload")
@EqualsAndHashCode(exclude = "payload")
public class BaseObject implements Serializable, Hashable {

    @Setter
    private Long id;
    private String topic;
    private int version;
    private String hash; // for privacy guarantee should be signed

    private String payload;

    public static BaseObjectBuilder builder() {return new BaseObjectBuilder();}

    @Override
    public String hash() {
        return CryptoUtils.sha256(payload);
    }

    public static class BaseObjectBuilder {
        private Long id;
        private String topic;
        private int version;
        private String payload;

        BaseObjectBuilder() {}

        public BaseObjectBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BaseObjectBuilder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public BaseObjectBuilder version(int version) {
            this.version = version;
            return this;
        }

        public BaseObjectBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public BaseObject build() {
//            return new BaseObject(id, topic, version, CryptoUtils.sha256(payload), payload);
            return new BaseObject(id, topic, 0, null, payload);
        }

        public String toString() {return "BaseObject.BaseObjectBuilder(id=" + this.id + ", topic=" + this.topic + ", version=" + this.version  + ", payload=" + this.payload + ")";}
    }
}
