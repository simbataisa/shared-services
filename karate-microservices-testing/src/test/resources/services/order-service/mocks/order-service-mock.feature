Feature: Order Service Mock

Scenario: pathMatches('/orders') && methodIs('post')
  * def response = { id: 1, status: 'CREATED', userId: request.userId, sku: request.sku, quantity: request.quantity }
  * def responseStatus = 201

Scenario: pathMatches('/orders/{id}') && methodIs('get')
  * def id = pathParams.id
  * def response = id == '1' ? { id: 1, status: 'CREATED', userId: 1, sku: 'ABC-123', quantity: 1 } : { error: { code: 'NOT_FOUND', message: 'Order not found' } }
  * def responseStatus = id == '1' ? 200 : 404