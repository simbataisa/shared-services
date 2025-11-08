package com.ahss.automation.helpers;

public class CustomValidators {
  public static boolean isNonEmpty(String s) {
    return s != null && !s.trim().isEmpty();
  }
  public static boolean isPositive(int n) {
    return n > 0;
  }
}