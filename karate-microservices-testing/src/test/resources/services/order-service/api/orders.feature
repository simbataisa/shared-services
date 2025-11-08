Feature: Order Service API

Background:
  * def token = call read('classpath:common/auth/oauth2.feature@getAccessToken')
  * url baseUrl + '/orders'
  * header Authorization = 'Bearer ' + token.accessToken

@smoke
Scenario: Create order
  Given request { userId: 1, sku: 'ABC-123', quantity: 1 }
  When method post
  Then status 201
  And match response contains { id: '#number', status: 'CREATED' }

Scenario: Get order
  Given path '1'
  When method get
  Then status 200
  And match response contains { id: 1, status: 'CREATED' }