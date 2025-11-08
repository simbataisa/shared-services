Feature: User Service API

Background:
  * def token = call read('classpath:common/auth/oauth2.feature@getAccessToken')
  * url baseUrl + '/users'
  * header Authorization = 'Bearer ' + token.accessToken
  * def expected = read('classpath:common/schemas/user-schema.json')

@smoke
Scenario: Create user
  Given request { name: 'John Doe', email: 'john@example.com' }
  When method post
  Then status 201
  And match response == expected
  And match response.name == 'John Doe'

Scenario Outline: Get user by id
  Given path '<id>'
  When method get
  Then status <code>

  Examples:
    | id   | code |
    | 1    | 200  |
    | 9999 | 404  |