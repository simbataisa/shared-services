package com.ahss.automation.runners.api;

import com.intuit.karate.junit5.Karate;

public class UsersApiRunner {

  @Karate.Test
  Karate testUsersApi() {
    return Karate.run("classpath:api/users.feature");
  }
}