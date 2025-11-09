Feature: Verification Helpers — Verify User Access

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    # Allow injected auth / headers; else fallback to login + common headers
    * def userId = karate.get('userId')
    * def username = karate.get('username')
    * def password = karate.get('password')
    * if (!userId || !username || !password) karate.abort()
    * def login = call read('classpath:common/auth/login.feature') { username: #(username), password: #(password) }
    * def auth = { token: '#(login.response.data.token)' }
    * print 'Login response:', login.response.data.token
    * print 'Auth:', auth
    * configure headers = read('classpath:common/headers/common-headers.js')
    * def utils = karate.get('utils')

    @verifyUserAccess
  Scenario: Verify user can access own record
    # Always login using provided username/password
    * def token = auth.token
    Given path '/api/v1/users', userId
    And header Authorization = 'Bearer ' + token
    * print 'Verify access with token for userId:', userId
    When method get
    * print 'Verify access response:', response
    Then status 200
    And match response.success == true
    And match response.data.id == userId
    * def result = { verified: true, userId: '#(userId)' }  
