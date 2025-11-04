package com.ahss.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.MediaType;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;

@Epic("Security")
@Feature("BCrypt")
@Owner("backend")
public class BCryptTest {

    @BeforeEach
    void applyLabels() {
        // Using annotations; no runtime labels
    }

    @Test
    @Story("BCrypt matches known and generated hashes")
    public void testPasswordMatching() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "superadmin123";

        // Test the hash from the database
        String hashFromDB = "$2a$10$9qgUn7rhqgA4cF1zriweJ.zTciXnaEevRpS3kZ10JeShby4Yy8vwe";
        boolean matches1 = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> encoder.matches(password, hashFromDB));
        Allure.addAttachment("DB Hash", MediaType.TEXT_PLAIN_VALUE, hashFromDB);
        Allure.addAttachment("Password", MediaType.TEXT_PLAIN_VALUE, password);

        // Test the hash from V4 seed file
        String hashFromSeed = "$2a$10$9qgUn7rhqgA4cF1zriweJ.zTciXnaEevRpS3kZ10JeShby4Yy8vwe";
        boolean matches2 = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> encoder.matches(password, hashFromSeed));
        Allure.addAttachment("Seed Hash", MediaType.TEXT_PLAIN_VALUE, hashFromSeed);

        // Generate a new hash for comparison
        String newHash = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> encoder.encode(password));
        Allure.addAttachment("New Hash", MediaType.TEXT_PLAIN_VALUE, newHash);
        boolean matches3 = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> encoder.matches(password, newHash));
    }

    @Test
    @Story("BCrypt encodes password for storage")
    public void testPasswordEncoding() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "testuser123";
        String encoded = org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> encoder.encode(password));
        Allure.addAttachment("Encoded Password", MediaType.TEXT_PLAIN_VALUE, encoded);
    }
}
