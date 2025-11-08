package com.ahss.automation.runners.auth;

import com.intuit.karate.junit5.Karate;

public class LoginRunner {

  @Karate.Test
  Karate testLogin() {
    return Karate.run("classpath:common/auth/login.feature");
  }
}