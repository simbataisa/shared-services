Feature: Users API

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')
    * def utils = karate.get('utils')

  @ignore
  Scenario: Get all active users
    Given path '/api/v1/users'
    When method get
    Then status 200
    And match response.success == true
    And match response.message contains 'User'

  Scenario: Create, read, update, and delete a user
    * def unique = java.util.UUID.randomUUID().toString()
    * def newUser = { username: #('user_' + unique), email: #(utils.randomEmail()), password: 'Str0ngP@ss!', firstName: 'Test', lastName: 'User', phoneNumber: '1234567890' }
    Given path '/api/v1/users'
    And request newUser
    * print 'Create request:', newUser
    When method post
    * print 'Create response:', response
    Then status 201
    And match response.success == true
    * def userId = response.data.id

    # Read by id
    Given path '/api/v1/users', userId
    When method get
    * print 'Read response:', response
    Then status 200
    And match response.success == true
    And match response.data.id == userId

    # Update user
    * def updated = { username: #(newUser.username), email: #(newUser.email), firstName: 'Updated', lastName: 'User' }
    Given path '/api/v1/users', userId
    And request updated
    * print 'Update request:', updated
    When method put
    * print 'Update response:', response
    Then status 200
    And match response.success == true
    And match response.data.firstName == 'Updated'

    # Delete user
    Given path '/api/v1/users', userId
    When method delete
    Then status 200
    And match response.success == true

  @ignore
  Scenario: Get user by username
    * def name = 'admin'
    Given path '/api/v1/users/username', name
    When method get
    Then status 200
    And match response.success == true