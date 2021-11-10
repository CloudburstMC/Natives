package org.cloudburstmc.natives.util;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {

    private CryptoUtil() {
    }

    public static boolean isJCEUnlimitedStrength() {
        try {
            return Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE;
        } catch (NoSuchAlgorithmException e) {
            // AES should always exist.
            throw new AssertionError(e);
        }
    }
}
