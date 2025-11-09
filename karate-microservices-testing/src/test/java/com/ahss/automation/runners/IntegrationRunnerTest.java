package com.ahss.automation.runners;

import com.intuit.karate.junit5.Karate;
import com.intuit.karate.Runner;
import com.intuit.karate.core.MockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import java.util.stream.Stream;

class IntegrationRunnerTest {
  private static MockServer mockServer;

  @BeforeAll
  static void startMockIfNeeded() {
    // Start mock server when not pointing to a real endpoint
    String useMock = System.getProperty("use.mock", System.getenv().getOrDefault("USE_MOCK", "true"));
    if ("true".equalsIgnoreCase(useMock)) {
      int port = Integer.parseInt(System.getProperty("mock.port", System.getenv().getOrDefault("MOCK_PORT", "8090")));
      mockServer = MockServer.feature("src/test/resources/mocks/mock-server.feature").http(port).build();
      System.setProperty("BASE_URL", "http://localhost:" + mockServer.getPort());
      System.setProperty("AUTH_BASE_URL", "http://localhost:" + mockServer.getPort() + "/auth");
      System.setProperty("karate.env", System.getProperty("karate.env", "qa")); // default to qa against mock
    }
  }

  @AfterAll
  static void stopMock() {
    if (mockServer != null) {
      mockServer.stop();
    }
  }

  @Karate.Test
  Karate runAll() {
    String includeTags = System.getProperty("include.tags", System.getenv().getOrDefault("INCLUDE_TAGS", ""));
    String excludeTags = System.getProperty("exclude.tags", System.getenv().getOrDefault("EXCLUDE_TAGS", "~@ignore"));
    String[] include = includeTags.isBlank() ? new String[]{} : includeTags.split(",");
    String[] exclude = excludeTags.isBlank() ? new String[]{} : excludeTags.split(",");
    return Karate.run("classpath:services", "classpath:integration")
      .tags(Stream.concat(Stream.of(include), Stream.of(exclude)).toArray(String[]::new))
      .karateEnv(System.getProperty("karate.env", "dev"));
  }

  public static void main(String[] args) {
    Runner.path("classpath:services", "classpath:integration")
      .outputCucumberJson(true)
      .parallel(Integer.parseInt(System.getProperty("threads", "5")));
  }
}