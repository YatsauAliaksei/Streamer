package by.mrj.utils;

import by.mrj.domain.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class NetUtils {

    public static final String MAGIC = "MAGIC";

    public static byte[] serialize(Message<?> object) { // xxx: Possible should work only with Message type. Some for below.
        return serialize(object, true);
    }

    public static byte[] serialize(Serializable object, boolean withMagic) { // xxx: Possible should work only with Message type. Some for below.
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

//            if (withMagic)
//                oos.writeBytes(MAGIC); // first step check

            oos.writeObject(object);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return clazz.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Serializable> Message<T> deserialize(InputStream is, Class<T> clazz) {
//        try (ObjectInputStream ois = new ObjectInputStream(is)) {
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
//            checkMagic(ois);
            Message<T> message = (Message<T>) ois.readObject();
            return message; // command check to know validations to be performed.
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

//    public static Message<?> deserializeMessage(byte[] bytes) {
//        return deserializeMessage(new ByteArrayInputStream(bytes));
//    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close(); // no synchronization
            } catch (IOException ignored) {
            }
        }
    }

    // Works only with Message deserialization
    private static void checkMagic(ObjectInputStream in) throws IOException {
        byte[] buf = new byte[MAGIC.length()];
        in.read(buf, 0, buf.length);

        if (!Arrays.equals(MAGIC.getBytes(StandardCharsets.UTF_8), buf)) {
            in.close();
            throw new RuntimeException("Wrong magic"); // todo
        }
    }
}
