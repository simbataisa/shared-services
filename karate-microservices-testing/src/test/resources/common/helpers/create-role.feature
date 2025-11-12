Feature: Role Helpers — Create Role

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    # Allow injected auth / headers; else fallback to login + common headers
    * def providedAuth = karate.get('auth')
    * def providedHeaders = karate.get('headers')
    * def hasValidAuth = providedAuth && providedAuth.token && (providedAuth.token + '') != ''
    * def hasValidHeaders = providedHeaders && providedHeaders.Authorization && (providedHeaders.Authorization + '').startsWith('Bearer ')
    * print 'providedAuth valid:', hasValidAuth, 'providedHeaders valid:', hasValidHeaders
    * def login = (!hasValidAuth || !hasValidHeaders) ? karate.callSingle('classpath:common/auth/login.feature') : null
    * def auth = hasValidAuth ? providedAuth : { token: login.token }
    * configure headers = (hasValidAuth && hasValidHeaders) ? providedHeaders : read('classpath:common/headers/common-headers.js')

    @createRole
  Scenario: Create a role (supports overrides)
    * def unique = karate.get('unique', java.util.UUID.randomUUID().toString())
    * def defaultRoleName = 'ROLE_' + unique
    * def name = karate.get('name', defaultRoleName)
    * def description = karate.get('description', 'E2E temp role')
    * def permissionIds = karate.get('permissionIds', [])
    * def payload = { name: #(name), description: #(description), permissionIds: #(permissionIds) }
    Given path '/api/v1/roles'
    And request payload
    * print 'Create role request:', payload
    When method post
    * print 'Create role response:', response
    Then status 201
    And match response.success == true
    * def roleId = response.data.id
    * def result = { roleId: roleId }