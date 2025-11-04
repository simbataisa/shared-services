# Spring Security JWT Filter, UsernamePasswordAuthenticationFilter, and Swagger OpenAPI Authorization

## Overview
- This document explains how JWT-based authentication integrates into the Spring Security filter chain via `JwtAuthenticationFilter`, how it relates to `UsernamePasswordAuthenticationFilter`, and how Swagger/OpenAPI is configured with a global bearer security requirement to enable the Swagger UI "Authorize" workflow.
- It references the backend project in `sample/shared-services/backend` and uses the configuration currently present in the codebase.

## Key Components
- `SecurityConfig` (`com.ahss.config.SecurityConfig`)
  - Configures HTTP security rules, exposes public endpoints, and registers filters.
  - Adds the JWT filter before `UsernamePasswordAuthenticationFilter`:
    - `http.addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);`
- `JwtAuthenticationFilter` (`com.ahss.security.JwtAuthenticationFilter`)
  - Intercepts requests to extract and validate JWT from the `Authorization` header.
  - On successful validation, builds a Spring `Authentication` and stores it in the `SecurityContext` for downstream authorization.
  - Common responsibilities:
    - Read header `Authorization: Bearer <token>`
    - Validate token signature and expiry
    - Resolve user details and authorities
    - Populate `SecurityContextHolder` so controllers see an authenticated principal
- `UsernamePasswordAuthenticationFilter`
  - A standard Spring Security filter that handles form-login username/password submissions.
  - In JWT-based APIs, it typically isn’t used directly; placing `JwtAuthenticationFilter` before it ensures JWTs are evaluated early for stateless auth.
- `OpenApiConfig` (`com.ahss.config.OpenApiConfig`)
  - Declares a `bearerAuth` security scheme (HTTP `bearer`, format `JWT`).
  - Adds a global OpenAPI customizer to apply the `bearerAuth` security requirement to operations, so the Swagger UI shows the "Authorize" button consistently.

## Security Flow
- Incoming request → `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`.
- If `Authorization` header contains a valid Bearer token:
  - Token is parsed and validated.
  - `Authentication` is set in `SecurityContextHolder`.
  - Request continues with an authenticated principal.
- If token is missing or invalid:
  - Request proceeds without authentication; endpoints annotated or configured to require auth will return 401/403.

## Swagger/OpenAPI Configuration
- Dependencies in `build.gradle`:
  - `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0`
  - `io.swagger.core.v3:swagger-annotations:2.2.21`
- Security scheme in `OpenApiConfig`:
  - Name: `bearerAuth`
  - Type: HTTP, Scheme: `bearer`, Format: `JWT`
- Global security requirement:
  - An `OpenApiCustomizer` adds `bearerAuth` to all operations that don’t already declare it.
- Controller annotations use fully-qualified Swagger references to avoid name conflicts with `com.ahss.dto.response.ApiResponse`.

## Using the Swagger UI "Authorize" Button
- Start the backend: `./gradlew bootRun --args='--server.port=8082'`
- Open Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- Click "Authorize" next to `bearerAuth`.
  - Enter the raw JWT token (without the word `Bearer`); Swagger will send `Authorization: Bearer <token>` automatically.
- Obtain a token via login:
  - `POST /api/v1/auth/login` with JSON body:
    ```json
    {
      "username": "admin@ahss.com",
      "password": "Admin@123"
    }
    ```
  - The response includes `{ "token": "<JWT>" }`.
  - Paste the `<JWT>` into the Swagger UI authorization dialog.

## Security Rules Summary
- Public endpoints (examples):
  - `/api/v1/auth/**` and Swagger assets (`/v3/api-docs/**`, `/swagger-ui/**`) are permitted.
- Protected endpoints:
  - All other API routes require a valid JWT and appropriate authorities.
- Filter order:
  - `JwtAuthenticationFilter` executes before `UsernamePasswordAuthenticationFilter` to establish stateless authentication from Bearer tokens.

## Common Issues and Fixes
- Ambiguous `ApiResponse` type:
  - Controllers should reference Swagger annotations with fully-qualified names to avoid conflicts with `com.ahss.dto.response.ApiResponse`.
- Swagger security not appearing:
  - Ensure `OpenApiConfig` defines `@SecurityScheme(bearerAuth)` and the global customizer is active.
  - Confirm `springdoc` configuration in `application.yml` and that controllers are under `com.ahss.controller`.
- Token not applied from Swagger UI:
  - Verify you entered the raw token and not prefixed `Bearer`; the UI adds the scheme.
  - Check CORS/security configs if calling from external UIs.

## Reference Snippets
- Add JWT filter before username/password:
  ```java
  http.addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
  ```
- Security scheme declaration:
  ```java
  @SecurityScheme(
      name = "bearerAuth",
      type = SecuritySchemeType.HTTP,
      scheme = "bearer",
      bearerFormat = "JWT"
  )
  ```
- Global security customizer:
  ```java
  @Bean
  public OpenApiCustomizer globalSecurityCustomizer() {
      return openApi -> {
          SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");
          if (openApi.getPaths() != null) {
              openApi.getPaths().values().forEach(pathItem ->
                  pathItem.readOperations().forEach(operation -> {
                      boolean alreadyPresent = operation.getSecurity() != null &&
                          operation.getSecurity().stream().anyMatch(req -> req.containsKey("bearerAuth"));
                      if (!alreadyPresent) {
                          operation.addSecurityItem(securityRequirement);
                      }
                  })
              );
          }
      };
  }
  ```

## Validation Steps
- Build: `./gradlew clean build -x test`
- Run: `./gradlew bootRun --args='--server.port=8082'`
- Swagger UI loads and shows the "Authorize" button; after authorizing with a valid token, protected endpoints return data instead of 401.

