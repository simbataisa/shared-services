Feature: User Helpers — Create User

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
    * def utils = karate.get('utils')

    @createUser
  Scenario: Create a user (supports overrides)
    * def unique = karate.get('unique', java.util.UUID.randomUUID().toString())
    * print 'ProvidedHeaders:', providedHeaders
    * def defaultUsername = 'user_' + unique
    * def defaultEmail = utils ? utils.randomEmail() : defaultUsername + '@example.test'
    * def username = karate.get('username', defaultUsername)
    * def email = karate.get('email', defaultEmail)
    * def password = karate.get('password', 'Str0ngP@ss!')
    * def firstName = karate.get('firstName', 'E2E')
    * def lastName = karate.get('lastName', 'User')
    * def payload = { username: #(username), email: #(email), password: #(password), firstName: #(firstName), lastName: #(lastName) }
    Given path '/api/v1/users'
    And request payload
    * print 'Create user request:', payload
    When method post
    * print 'Create user response:', response
    Then status 201
    And match response.success == true
    * def userId = response.data.id
    * def username = payload.username
    * def password = payload.password
    * def result = { userId: userId, username: username, password: password }