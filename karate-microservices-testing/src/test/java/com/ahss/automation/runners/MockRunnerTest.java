package com.ahss.automation.runners;

import com.intuit.karate.core.Feature;
import com.intuit.karate.core.MockHandler;
import com.intuit.karate.http.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test runner for Karate Mock Server
 *
 * Starts a mock server that simulates external payment gateway APIs.
 * The mock server will run for the duration specified by mock.block.ms
 * system property or MOCK_BLOCK_MS environment variable.
 *
 * Usage:
 *   ./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa
 *   ./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.port=8090
 *   ./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=300000
 */
public class MockRunnerTest {

    private static final Logger log = LoggerFactory.getLogger(MockRunnerTest.class);
    private static HttpServer server;
    private static int serverPort;

    @BeforeAll
    static void setup() {
        log.info("=================================================");
        log.info("Setting up Karate Mock Server");
        log.info("=================================================");

        // Get configuration from system properties or environment variables
        serverPort = Integer.parseInt(
            System.getProperty("mock.port",
            System.getenv().getOrDefault("MOCK_PORT", "8090"))
        );

        String karateEnv = System.getProperty("karate.env", "qa");
        log.info("Karate Environment: {}", karateEnv);
        log.info("Mock Server Port: {}", serverPort);
    }

    @Test
    @DisplayName("Start mock server for payment gateways")
    void startMockServer() throws Exception {
        // Get feature file path from system property or environment variable
        String featureFilePath = System.getProperty("mock.feature.file",
            System.getenv().getOrDefault("MOCK_FEATURE_FILE", "src/test/resources/mocks/mock-server.feature")
        );
        File featureFile = new File(featureFilePath);

        log.info("Starting mock server with feature: {}", featureFile.getAbsolutePath());
        log.info("=================================================");

        try {
            // Load the feature file
            Feature feature = Feature.read(featureFile);
            MockHandler handler = new MockHandler(feature);

            // Build and start the mock server
            server = HttpServer
                    .handler(handler)
                    .http(serverPort)
                    .build();

            log.info("✓ Mock server started successfully");
            log.info("  URL: http://localhost:{}", server.getPort());
            log.info("=================================================");
            log.info("Available Mock Endpoints:");
            log.info("  Stripe (prefix: /stripe):");
            log.info("    - POST http://localhost:{}/stripe/v1/tokens", server.getPort());
            log.info("    - POST http://localhost:{}/stripe/v1/charges", server.getPort());
            log.info("    - GET  http://localhost:{}/stripe/v1/charges/{{id}}", server.getPort());
            log.info("    - POST http://localhost:{}/stripe/v1/refunds", server.getPort());
            log.info("");
            log.info("  PayPal (prefix: /paypal):");
            log.info("    - POST http://localhost:{}/paypal/v1/oauth2/token", server.getPort());
            log.info("    - POST http://localhost:{}/paypal/v2/checkout/orders", server.getPort());
            log.info("    - POST http://localhost:{}/paypal/v2/checkout/orders/{{id}}/capture", server.getPort());
            log.info("    - GET  http://localhost:{}/paypal/v2/checkout/orders/{{id}}", server.getPort());
            log.info("");
            log.info("  Bank Transfer (prefix: /bank-transfer):");
            log.info("    - POST http://localhost:{}/bank-transfer/api/v1/transfers", server.getPort());
            log.info("    - GET  http://localhost:{}/bank-transfer/api/v1/transfers/{{id}}", server.getPort());
            log.info("    - POST http://localhost:{}/bank-transfer/api/v1/accounts/verify", server.getPort());
            log.info("=================================================");

            // Verify server is running
            assertNotNull(server, "Server should not be null");
            assertEquals(serverPort, server.getPort(), "Server should be running on configured port");

            // Block to keep server alive for CI/local usage
            String blockMs = System.getProperty("mock.block.ms",
                System.getenv().getOrDefault("MOCK_BLOCK_MS", "600000"));
            long blockMillis = Long.parseLong(blockMs);

            log.info("Mock server will run for {} ms ({} minutes)",
                blockMillis, blockMillis / 60000);
            log.info("Press Ctrl+C to stop the server early");
            log.info("=================================================");

            Thread.sleep(blockMillis);

        } catch (Exception e) {
            log.error("Failed to start mock server", e);
            throw e;
        }
    }

    @AfterAll
    static void tearDown() {
        if (server != null) {
            log.info("=================================================");
            log.info("Stopping mock server...");
            server.stop();
            log.info("✓ Mock server stopped");
            log.info("=================================================");
        }
    }
}