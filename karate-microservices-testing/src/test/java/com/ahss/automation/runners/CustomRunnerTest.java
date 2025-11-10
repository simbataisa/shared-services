package com.ahss.automation.runners;

import com.intuit.karate.Runner;
import com.intuit.karate.junit5.Karate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class CustomRunnerTest {

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