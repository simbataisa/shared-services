package com.ahss.automation.runners;

import com.intuit.karate.Runner;
import com.intuit.karate.core.Feature;
import com.intuit.karate.core.MockHandler;
import com.intuit.karate.http.HttpServer;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CustomRunnerTest {

  private static final Logger log = LoggerFactory.getLogger(CustomRunnerTest.class);
  private static HttpServer mockServer;
  private static boolean mockServerEnabled;
  private static int mockServerPort;

  @BeforeAll
  public static void startMockServer() {
    // Check if mock server should be started
    mockServerEnabled = Boolean.parseBoolean(
        System.getProperty("mock.server.enabled",
        System.getenv().getOrDefault("MOCK_SERVER_ENABLED", "false"))
    );

    if (!mockServerEnabled) {
      log.info("Mock server is disabled. Set mock.server.enabled=true or MOCK_SERVER_ENABLED=true to enable it.");
      return;
    }

    mockServerPort = Integer.parseInt(
        System.getProperty("mock.port",
        System.getenv().getOrDefault("MOCK_PORT", "8090"))
    );

    log.info("=================================================");
    log.info("Starting Karate Mock Server");
    log.info("=================================================");
    log.info("Mock Server Port: {}", mockServerPort);

    try {
      File featureFile = new File("src/test/resources/mocks/mock-server.feature");
      if (!featureFile.exists()) {
        log.error("Mock server feature file not found: {}", featureFile.getAbsolutePath());
        return;
      }

      Feature feature = Feature.read(featureFile);
      MockHandler handler = new MockHandler(feature);

      mockServer = HttpServer
          .handler(handler)
          .http(mockServerPort)
          .build();

      log.info("✓ Mock server started successfully");
      log.info("  URL: http://localhost:{}", mockServer.getPort());
      log.info("=================================================");
      log.info("Available Mock Endpoints:");
      log.info("  Stripe (prefix: /stripe):");
      log.info("    - POST http://localhost:{}/stripe/v1/tokens", mockServer.getPort());
      log.info("    - POST http://localhost:{}/stripe/v1/charges", mockServer.getPort());
      log.info("  PayPal (prefix: /paypal):");
      log.info("    - POST http://localhost:{}/paypal/v1/oauth2/token", mockServer.getPort());
      log.info("    - POST http://localhost:{}/paypal/v2/checkout/orders", mockServer.getPort());
      log.info("  Bank Transfer (prefix: /bank-transfer):");
      log.info("    - POST http://localhost:{}/bank-transfer/api/v1/transfers", mockServer.getPort());
      log.info("=================================================");

    } catch (Exception e) {
      log.error("Failed to start mock server", e);
      throw new RuntimeException("Failed to start mock server", e);
    }
  }

  @AfterAll
  public static void stopMockServer() {
    if (mockServer != null) {
      log.info("=================================================");
      log.info("Stopping mock server...");
      mockServer.stop();
      log.info("✓ Mock server stopped");
      log.info("=================================================");
    }
  }

  @Karate.Test
  Karate runApi() {
    String optionsStr = System.getProperty("karate.options", "");
    System.out.println("optionsStr " + optionsStr);
    if (optionsStr != null && !optionsStr.isBlank()) {
      System.out.println("karate.options: " + optionsStr);
      KarateOptions opts = KarateOptions.parse(optionsStr);
      String env = System.getProperty("karate.env", opts.env != null ? opts.env : "qa");
      String[] tagsArr = opts.tags.toArray(new String[0]);
      String[] pathsArr = opts.paths.isEmpty() ? new String[] { "classpath:api" } : opts.paths.toArray(new String[0]);
      return Karate.run(pathsArr)
          .tags(tagsArr)
          .karateEnv(env);
    } else {
      String includeTags = System.getProperty("include.tags", System.getenv().getOrDefault("INCLUDE_TAGS", ""));
      String excludeTags = System.getProperty("exclude.tags", System.getenv().getOrDefault("EXCLUDE_TAGS", "~@ignore"));
      String[] include = includeTags.isBlank() ? new String[] {} : includeTags.split(",");
      String[] exclude = excludeTags.isBlank() ? new String[] {} : excludeTags.split(",");
      return Karate.run("classpath:integration/payment-end-to-end-success.feature")
          .tags(Stream.concat(Stream.of(include), Stream.of(exclude)).toArray(String[]::new))
          .karateEnv(System.getProperty("karate.env", "qa"));
    }
  }

  public static void main(String[] args) {
    String optionsStr = System.getProperty("karate.options", "");
    KarateOptions opts = (optionsStr != null && !optionsStr.isBlank()) ? KarateOptions.parse(optionsStr)
        : new KarateOptions();
    int threads = Integer
        .parseInt(System.getProperty("threads", opts.threads != null ? String.valueOf(opts.threads) : "5"));

    Runner.Builder builder = Runner.builder();
    if (!opts.paths.isEmpty()) {
      builder.path(opts.paths.toArray(new String[0]));
    } else {
      builder.path("classpath:api");
    }
    if (!opts.tags.isEmpty()) {
      builder.tags(opts.tags.toArray(new String[0]));
    }
    builder.karateEnv(System.getProperty("karate.env", opts.env != null ? opts.env : "qa"));
    builder.outputCucumberJson(opts.cucumberJson != null ? opts.cucumberJson : true);

    builder.parallel(threads);
  }

  static class KarateOptions {
    List<String> paths = new ArrayList<>();
    List<String> tags = new ArrayList<>();
    Integer threads;
    String env;
    Boolean cucumberJson;

    static KarateOptions parse(String options) {
      KarateOptions ko = new KarateOptions();
      String[] tokens = options.trim().split("\\s+");
      for (int i = 0; i < tokens.length; i++) {
        String t = tokens[i];
        switch (t) {
          case "--tags":
            if (i + 1 < tokens.length) {
              String tagStr = tokens[++i];
              for (String tag : tagStr.split(",")) {
                if (!tag.isBlank())
                  ko.tags.add(tag.trim());
              }
            }
            break;
          case "--threads":
            if (i + 1 < tokens.length) {
              try {
                ko.threads = Integer.parseInt(tokens[++i]);
              } catch (NumberFormatException ignored) {
              }
            }
            break;
          case "--env":
          case "--karate.env":
            if (i + 1 < tokens.length) {
              ko.env = tokens[++i];
            }
            break;
          case "--cucumberJson":
          case "--outputCucumberJson":
            ko.cucumberJson = true;
            break;
          default:
            // Treat anything else as a path
            ko.paths.add(t);
        }
      }
      return ko;
    }
  }
}