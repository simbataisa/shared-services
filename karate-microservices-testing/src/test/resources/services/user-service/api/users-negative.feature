Feature: User Service Negative Tests

Background:
  * url baseUrl + '/users'

Scenario: Create user with invalid email
  Given request { name: 'Bad User', email: 'not-an-email' }
  When method post
  Then status 400
  And match response == read('classpath:common/schemas/error-schema.json')