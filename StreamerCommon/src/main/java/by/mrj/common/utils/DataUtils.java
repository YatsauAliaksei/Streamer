package by.mrj.common.utils;

import by.mrj.common.domain.data.BaseObject;
import by.mrj.common.serialization.json.JsonJackson;

public class DataUtils {

    public static BaseObject createNewData(String topic, String objUuid, Object data) {

        return BaseObject.builder()
                .topic(topic)
                .version(0)
                .uuid(objUuid)
                .payload(JsonJackson.toJson(data))
                .build();
    }
}
