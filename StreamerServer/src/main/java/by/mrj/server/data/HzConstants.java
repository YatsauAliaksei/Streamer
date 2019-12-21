package by.mrj.server.data;

public abstract class HzConstants {

    public interface Maps {

        String USER_TO_SUBSCRIPTION = "user_to_subscription";

        String SUBSCRIPTION_TO_USER = "subscription_to_user";

        String SUBSCRIPTION_TO_IDS = "subscription_to_ids";

        // topic metadata
        String TOPICS = "topics";

    }
//    user -> subscription |  subscription -> ids | topic -> objects

    public interface Locks {

        String USER_READ = "_user_read";

        String USER_FETCH = "_user_fetch";
    }

}
