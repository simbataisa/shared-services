Feature: User places order end-to-end

Background:
  * def token = call read('classpath:common/auth/oauth2.feature@getAccessToken')
  * header Authorization = 'Bearer ' + token.accessToken

@e2e
Scenario: Create user -> create order -> verify order status
  # Create user
  Given url baseUrl + '/users'
  And request { name: 'E2E User', email: 'e2e@example.com' }
  When method post
  Then status 201
  * def userId = response.id

  # Create order
  Given url baseUrl + '/orders'
  And request { userId: #(userId), sku: 'ABC-123', quantity: 1 }
  When method post
  Then status 201
  * def orderId = response.id

  # Verify order
  Given url baseUrl + '/orders/' + orderId
  When method get
  Then status 200
  And match response.status == 'CREATED'