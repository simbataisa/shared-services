package com.ahss.automation.runners.api;

import com.intuit.karate.junit5.Karate;

public class UserGroupsApiRunner {

  @Karate.Test
  Karate testUserGroupsApi() {
    return Karate.run("classpath:api/user-groups.feature");
  }
}