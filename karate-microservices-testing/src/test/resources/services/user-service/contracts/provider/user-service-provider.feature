Feature: Provider contract for user payload

Background:
  * configure headers = { Accept: 'application/json' }

@contract
Scenario: Provider exposes expected schema for /users/{id}
  Given url baseUrl + '/users/1'
  When method get
  Then status 200
  And match response == read('classpath:common/schemas/user-schema.json')