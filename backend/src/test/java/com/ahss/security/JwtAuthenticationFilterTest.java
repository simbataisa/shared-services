package com.ahss.security;

import com.ahss.dto.PermissionDto;
import com.ahss.dto.RoleDto;
import com.ahss.dto.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Security")
@Feature("Authentication Filter")
@Owner("backend")
class JwtAuthenticationFilterTest {

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter();
    SecurityContextHolder.clearContext();
    // Using annotations; no runtime labels
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private String tokenWithAdminRole() {
    UserDto user = new UserDto();
    user.setId(7L);
    user.setEmail("admin@ahss.com");
    user.setUsername("admin");
    user.setFirstName("Admin");
    user.setLastName("User");

    RoleDto admin = new RoleDto();
    admin.setName("System Administrator");
    PermissionDto p = new PermissionDto();
    p.setName("payments:read");
    admin.setPermissions(List.of(p));
    user.setRoles(List.of(admin));
    return JwtTokenProvider.generateTokenWithUserInfo(user);
  }

  private static class RecordingChain implements FilterChain {
    boolean proceeded = false;

    @Override
    public void doFilter(
        jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
      proceeded = true;
    }
  }

  @Test
  @Story("Valid token authenticates and sets authorities")
  @Severity(SeverityLevel.CRITICAL)
  void valid_token_populates_security_context_with_authorities()
      throws ServletException, IOException {
    String token = tokenWithAdminRole();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/payments");
    request.addHeader("Authorization", "Bearer " + token);
    Allure.addAttachment(
        "Authorization Header", MediaType.TEXT_PLAIN_VALUE, request.getHeader("Authorization"));
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingChain chain = new RecordingChain();

    Allure.step(
        "Execute filter chain",
        () -> {
          try {
            filter.doFilter(request, response, chain);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

    assertTrue(chain.proceeded, "Filter chain should proceed");
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth, "Authentication should be set");
    assertTrue(auth.getPrincipal() instanceof UserPrincipal, "Principal should be UserPrincipal");
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
    assertEquals("admin@ahss.com", principal.getUsername());
    assertEquals(7, principal.getUserId());
    assertTrue(
        auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMINISTRATOR")));
  }

  @Test
  @Story("Invalid token does not authenticate")
  @Severity(SeverityLevel.NORMAL)
  void invalid_token_does_not_authenticate_but_chain_proceeds()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/payments");
    request.addHeader("Authorization", "Bearer invalid.token.value");
    Allure.addAttachment(
        "Authorization Header", MediaType.TEXT_PLAIN_VALUE, request.getHeader("Authorization"));
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingChain chain = new RecordingChain();

    Allure.step(
        "Execute filter chain",
        () -> {
          try {
            filter.doFilter(request, response, chain);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

    assertTrue(chain.proceeded, "Filter chain should proceed");
    assertNull(
        SecurityContextHolder.getContext().getAuthentication(),
        "Authentication should not be set for invalid token");
  }

  @Test
  @Story("No Authorization header leaves context empty")
  @Severity(SeverityLevel.TRIVIAL)
  void no_authorization_header_leaves_context_empty() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public");
    MockHttpServletResponse response = new MockHttpServletResponse();
    RecordingChain chain = new RecordingChain();

    Allure.step(
        "Execute filter chain",
        () -> {
          try {
            filter.doFilter(request, response, chain);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

    assertTrue(chain.proceeded, "Filter chain should proceed");
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
