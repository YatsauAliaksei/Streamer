package by.mrj.common.utils;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class CryptoUtils {

    public static String doubleSha256(String toHash) {
        return sha256(sha256(toHash));
    }

    public static String sha256(String toHash) {
        return Hashing.sha256()
                .hashString(toHash, StandardCharsets.UTF_8)
                .toString();
    }
}
