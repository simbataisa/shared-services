package com.ahss.automation.runners;

import com.intuit.karate.core.MockServer;
import org.junit.jupiter.api.Test;

public class MockRunner {

  @Test
  void startMockServer() throws Exception {
    String featurePath = "src/test/resources/mocks/mock-server.feature";
    int port = Integer.parseInt(System.getProperty("mock.port", System.getenv().getOrDefault("MOCK_PORT", "8090")));
    MockServer server = MockServer.feature(featurePath).http(port).build();
    System.out.println("Mock server started at port: " + server.getPort());
    // Block to keep server alive for CI/local usage
    String blockMs = System.getProperty("mock.block.ms", System.getenv().getOrDefault("MOCK_BLOCK_MS", "600000"));
    Thread.sleep(Long.parseLong(blockMs)); // 10 minutes default
    server.stop();
  }
}