package com.ahss.automation.runners;

import com.intuit.karate.junit5.Karate;
import com.intuit.karate.Runner;
import java.util.stream.Stream;

class ApiRunnerTest {

  @Karate.Test
  Karate runApi() {
    String includeTags = System.getProperty("include.tags", System.getenv().getOrDefault("INCLUDE_TAGS", ""));
    String excludeTags = System.getProperty("exclude.tags", System.getenv().getOrDefault("EXCLUDE_TAGS", "~@ignore"));
    String[] include = includeTags.isBlank() ? new String[]{} : includeTags.split(",");
    String[] exclude = excludeTags.isBlank() ? new String[]{} : excludeTags.split(",");
    return Karate.run("classpath:api")
      .tags(Stream.concat(Stream.of(include), Stream.of(exclude)).toArray(String[]::new))
      .karateEnv(System.getProperty("karate.env", "qa"));
  }

  public static void main(String[] args) {
    Runner.path("classpath:api")
      .outputCucumberJson(true)
      .parallel(Integer.parseInt(System.getProperty("threads", "5")));
  }
}