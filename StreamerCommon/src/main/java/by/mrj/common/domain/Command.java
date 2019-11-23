package by.mrj.common.domain;

public enum Command {
    POST, READ_SPECIFIC, READ_ALL, SUBSCRIBE, UNSUBSCRIBE, CREATE_TOPIC;

    public static Command byOrdinal(int ordinal) {
        if (ordinal < Command.values().length) {
            return Command.values()[ordinal];
        }
        throw new UnsupportedOperationException("Command doesn't exist [" + ordinal + "]");
    }
}
