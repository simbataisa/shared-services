Feature: Authentication - Login

  Background:
    # Base URL for the backend auth API
    * def baseUrl = karate.get('authBaseUrl') || java.lang.System.getenv('AUTH_BASE_URL') || 'http://localhost:8080/api/v1/auth'
    # Allow credentials to be overridden; fall back to system properties or environment variables
    * def username = karate.get('username') || karate.properties['USERNAME'] || java.lang.System.getenv('USERNAME') || 'superadmin@ahss.com'
    * def password = karate.get('password') || karate.properties['PASSWORD'] || java.lang.System.getenv('PASSWORD') || 'superadmin123'
  
    @name=getAccessToken
  Scenario: Valid admin login returns JWT token
    Given url baseUrl
    And print 'Auth base:', baseUrl
    And path 'login'
    And header Content-Type = 'application/json'
    And request { username: '#(username)', password: '#(password)' }
    When method post
    And print 'Login response:', response
    Then status 200
    And match response.success == true
    And match response.message contains 'Login'
    And match response.data.token != null
    * def token = response.data.token
    And assert token.length > 20
