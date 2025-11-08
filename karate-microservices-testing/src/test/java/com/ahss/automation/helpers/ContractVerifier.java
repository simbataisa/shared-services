package com.ahss.automation.helpers;

import java.util.Map;

public class ContractVerifier {
  public boolean verifyShape(Map<String, Object> payload, Map<String, Object> shape) {
    // Simplistic shape verification placeholder
    for (String key : shape.keySet()) {
      if (!payload.containsKey(key)) return false;
    }
    return true;
  }
}