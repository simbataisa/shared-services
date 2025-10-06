package com.ahss.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTest {

    @Test
    public void testPasswordMatching() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "superadmin123";

        // Test the hash from the database
        String hashFromDB = "$2a$10$9qgUn7rhqgA4cF1zriweJ.zTciXnaEevRpS3kZ10JeShby4Yy8vwe";
        boolean matches1 = encoder.matches(password, hashFromDB);
        System.out.println("Hash from DB matches 'superadmin123': " + matches1);

        // Test the hash from V4 seed file
        String hashFromSeed = "$2a$10$9qgUn7rhqgA4cF1zriweJ.zTciXnaEevRpS3kZ10JeShby4Yy8vwe";
        boolean matches2 = encoder.matches(password, hashFromSeed);
        System.out.println("Hash from seed matches 'superadmin123': " + matches2);

        // Generate a new hash for comparison
        String newHash = encoder.encode(password);
        System.out.println("New hash for 'superadmin123': " + newHash);
        boolean matches3 = encoder.matches(password, newHash);
        System.out.println("New hash matches 'superadmin123': " + matches3);
    }

    @Test
    public void testPasswordEncoding() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "testuser123";

        encoder.encode(password);
        System.out.println("Encoded password for 'demo123': " + encoder.encode(password));
    }
}
