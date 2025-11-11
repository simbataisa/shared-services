package com.ahss.automation.runners.api;

import com.intuit.karate.junit5.Karate;

public class TenantApiRunner {

  @Karate.Test
  Karate testTenantsApi() {
    return Karate.run("classpath:api/tenants.feature");
  }
}