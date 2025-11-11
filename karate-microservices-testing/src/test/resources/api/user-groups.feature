Feature: User Groups API

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')

  Scenario: List user groups
    Given path '/api/v1/user-groups'
    When method get
    Then status 200
    And match response.success == true

  Scenario: Create, read, update, and delete a user group
    * def groupName = 'Group ' + java.util.UUID.randomUUID()
    * def createReq = { name: #(groupName), description: 'Test Group' }
    Given path '/api/v1/user-groups'
    And request createReq
    When method post
    Then status 201
    And match response.success == true
    * def groupId = response.data.userGroupId

    # Read by ID
    Given path '/api/v1/user-groups', groupId
    When method get
    Then status 200
    And match response.success == true

    # Update
    * def updateReq = { name: #(groupName), description: 'Updated Group' }
    Given path '/api/v1/user-groups', groupId
    And request updateReq
    * print 'Update request:', updateReq
    When method put
    * print 'Update response:', response
    Then status 200
    And match response.success == true

    # Delete
    Given path '/api/v1/user-groups', groupId
    When method delete
    Then status 200
    And match response.success == true