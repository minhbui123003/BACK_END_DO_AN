package com.TTT.Tniciu_API.Utils;

import java.util.Random;

public class RandomIdGenerator {

    private static final String ALLOWED_CHARACTERS = "abcdef0123456789";

    public static String generateRandomId(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            sb.append(ALLOWED_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}