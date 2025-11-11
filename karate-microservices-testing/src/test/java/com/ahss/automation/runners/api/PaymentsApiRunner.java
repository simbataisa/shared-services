package com.ahss.automation.runners.api;

import com.intuit.karate.junit5.Karate;

public class PaymentsApiRunner {

  @Karate.Test
  Karate testPaymentsApi() {
    return Karate.run("classpath:api/payments.feature");
  }
}