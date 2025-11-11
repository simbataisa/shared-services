package com.ahss.karate.mocks;

import com.intuit.karate.core.Feature;
import com.intuit.karate.core.MockHandler;
import com.intuit.karate.http.HttpServer;

import java.io.File;

/**
 * Standalone Karate Mock Server Runner for Payment Gateway Mocks
 *
 * This starts a mock server on port 8090 that simulates:
 * - Stripe API (tokenization & charges)
 * - PayPal API (orders & captures)
 * - Bank Transfer API
 *
 * To start the mock server:
 * 1. Run this class directly: java -cp ... com.ahss.karate.mocks.MockServerRunner
 * 2. Or via Gradle: ./gradlew mockServer
 * 3. Server will start on http://localhost:8090
 *
 * To use with backend integration tests:
 * - Start this mock server first
 * - Start backend with: ./gradlew bootRun --args='--spring.profiles.active=integration'
 * - Backend will use mock URLs configured in application-integration.yml
 */
public class MockServerRunner {

    private static final int MOCK_SERVER_PORT = 8090;
    private static final String MOCK_FEATURE_FILE = "src/test/resources/mocks/mock-server.feature";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("Starting Karate Mock Server for Payment Gateways");
        System.out.println("=================================================");
        System.out.println("Port: " + MOCK_SERVER_PORT);
        System.out.println("Mock Feature: " + MOCK_FEATURE_FILE);
        System.out.println();
        System.out.println("Available Mock Endpoints:");
        System.out.println("  Stripe (prefix: /stripe):");
        System.out.println("    - POST http://localhost:8090/stripe/v1/tokens (Tokenization)");
        System.out.println("    - POST http://localhost:8090/stripe/v1/charges (Process Payment)");
        System.out.println("    - GET  http://localhost:8090/stripe/v1/charges/{id}");
        System.out.println("    - POST http://localhost:8090/stripe/v1/refunds");
        System.out.println();
        System.out.println("  PayPal (prefix: /paypal):");
        System.out.println("    - POST http://localhost:8090/paypal/v1/oauth2/token");
        System.out.println("    - POST http://localhost:8090/paypal/v2/checkout/orders");
        System.out.println("    - POST http://localhost:8090/paypal/v2/checkout/orders/{id}/capture");
        System.out.println("    - GET  http://localhost:8090/paypal/v2/checkout/orders/{id}");
        System.out.println("    - POST http://localhost:8090/paypal/v2/payments/captures/{id}/refund");
        System.out.println();
        System.out.println("  Bank Transfer (prefix: /bank-transfer):");
        System.out.println("    - POST http://localhost:8090/bank-transfer/api/v1/transfers");
        System.out.println("    - GET  http://localhost:8090/bank-transfer/api/v1/transfers/{id}");
        System.out.println("    - POST http://localhost:8090/bank-transfer/api/v1/accounts/verify");
        System.out.println("    - POST http://localhost:8090/bank-transfer/api/v1/transfers/{id}/cancel");
        System.out.println();
        System.out.println("=================================================");
        System.out.println("Press Ctrl+C to stop the server");
        System.out.println("=================================================");
        System.out.println();

        try {
            // Load the feature file
            File featureFile = new File(MOCK_FEATURE_FILE);
            Feature feature = Feature.read(featureFile);
            MockHandler handler = new MockHandler(feature);

            // Start the mock server
            HttpServer server = HttpServer
                    .handler(handler)
                    .http(MOCK_SERVER_PORT)
                    .build();

            // Keep the server running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down mock server...");
                server.stop();
                System.out.println("Mock server stopped.");
            }));

            // Wait indefinitely
            synchronized (MockServerRunner.class) {
                try {
                    MockServerRunner.class.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to start mock server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
