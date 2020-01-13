package by.mrj.common.utils;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DataUtils {

    public static BaseObject createNewData(String topic, Integer id, Object data) {

        return BaseObject.builder()
                .topic(topic)
//                .version(0)
                .id(id)
                .payload(JsonJackson.toJson(data))
                .build();
    }

    public static Map<String, List<Integer>> topicToIds(Collection<BaseObject> objects, Function<String, String> transformer) {

        return objects.stream()
                .collect(Collectors.groupingBy(bo -> transformer.apply(bo.getTopic()),
                        Collector.of(ArrayList::new,
                                (id, bo) -> id.add(bo.getId()),
                                (left, right) -> {
                                    left.addAll(right);
                                    return left;
                                },
                                Collector.Characteristics.IDENTITY_FINISH)));
    }
}
