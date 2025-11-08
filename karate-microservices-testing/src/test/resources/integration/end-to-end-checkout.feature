Feature: End-to-end checkout flow

@e2e
Scenario: Checkout success path
  Given url baseUrl + '/users'
  And request { name: 'Checkout User', email: 'checkout@example.com' }
  When method post
  Then status 201
  * def userId = response.id

  Given url baseUrl + '/orders'
  And request { userId: #(userId), sku: 'SKU-001', quantity: 2 }
  When method post
  Then status 201
  * def orderId = response.id

  Given url baseUrl + '/orders/' + orderId
  When method get
  Then status 200
  And match response.status == 'CREATED'