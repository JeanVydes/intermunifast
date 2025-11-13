package com.example.utils;

import java.security.SecureRandom;

public final class OtpUtil {
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpUtil() {}

    public static String generateBase32Otp(int length) {
        if (length <= 0) throw new IllegalArgumentException("length must be > 0");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RANDOM.nextInt(BASE32_CHARS.length());
            sb.append(BASE32_CHARS.charAt(idx));
        }
        return sb.toString();
    }
}
