Feature: Role Assignment Helpers — Assign Role to User

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    # Allow injected auth / headers; else fallback to login + common headers
    * def providedAuth = karate.get('auth')
    * def login = providedAuth ? null : karate.callSingle('classpath:common/auth/login.feature')
    * def auth = providedAuth ? providedAuth : { token: login.token }
    * def providedHeaders = karate.get('headers')
    * configure headers = providedHeaders ? providedHeaders : read('classpath:common/headers/common-headers.js')

  @assignRoleToUser
  Scenario: Assign role(s) to a user
    * def userId = karate.get('userId')
    * def roleIds = karate.get('roleIds', karate.get('roleId') ? [ karate.get('roleId') ] : null)
    * if (!userId || !roleIds) karate.abort()
    Given path '/api/v1/users', userId, 'roles'
    And request roleIds
    * print 'Assign role(s) request:', roleIds, 'userId:', userId
    When method post
    * print 'Assign role(s) response:', response
    Then status 200
    And match response.success == true
    * def result = { success: response.success, userId: userId, roleIds: roleIds, roleId: karate.get('roleId') }