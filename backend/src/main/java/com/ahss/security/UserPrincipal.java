package com.ahss.security;

import lombok.Getter;

import java.io.Serializable;
import java.security.Principal;

@Getter
public class UserPrincipal implements Principal, Serializable {

  private final Long userId;
  private final String username;

  public UserPrincipal(Long userId, String username) {
    this.userId = userId;
    this.username = username;
  }

  @Override
  public String getName() {
    return username;
  }

  @Override
  public String toString() {
    return "UserPrincipal{" + "userId=" + userId + ", username='" + username + '\'' + '}';
  }
}
