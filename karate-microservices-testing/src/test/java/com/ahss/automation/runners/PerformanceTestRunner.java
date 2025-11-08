package com.ahss.automation.runners;

import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

public class PerformanceTestRunner {
  @Test
  void performance() {
    Runner.path("classpath:services/user-service/performance", "classpath:services/order-service/performance")
      .outputCucumberJson(true)
      .parallel(Integer.parseInt(System.getProperty("threads", "10")));
  }
}