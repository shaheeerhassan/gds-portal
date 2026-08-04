package com.school.utils;


import java.security.SecureRandom;

public class UsernameGenerator {

    private static final SecureRandom random = new SecureRandom();

    private UsernameGenerator() {}

    private static final String[] DICTIONARY1 = {
            "person", "user", "human", "man", "soul", "creature", "being", "curiosity", "knowledge",
            "seeker", "mind", "shadow", "runner", "spark", "echo", "spirit", "scout", "vertex"
    };

    private static final String[] DICTIONARY2 = {
            "basket", "tree", "flag", "rain", "butterfly", "thing", "net", "wire", "drop",
            "cloud", "stone", "river", "wind", "ocean", "star", "fire", "leaf", "pixel"
    };

    private static String getRandomWord() {

        int randomIndex1 = random.nextInt(DICTIONARY1.length);
        int randomIndex2 = random.nextInt(DICTIONARY2.length);

        return DICTIONARY1[randomIndex1] + "_" + DICTIONARY2[randomIndex2];
    }

    public static String generateUsername() {
        return getRandomWord() + "_" + random.nextInt(1000000);
    }
}
