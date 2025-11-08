Feature: Local mock for external deps and services

Background:
  * def uuid = function(){ return java.util.UUID.randomUUID() + '' }

# OAuth token
Scenario: pathMatches('/auth/oauth/token') && methodIs('post')
  * def response = { access_token: uuid(), token_type: 'bearer', expires_in: 3600 }
  * def responseStatus = 200

# Delegate to user-service mock
Scenario: pathMatches('/users') && methodIs('post')
  * def now = new java.util.Date().toString()
  * def response = { id: 1, name: request.name, email: request.email, createdAt: now, metadata: { version: '1.0', source: 'mock' } }
  * def responseStatus = 201

Scenario: pathMatches('/users/{id}') && methodIs('get')
  * def id = pathParams.id
  * def response = id == '1' ? { id: 1, name: 'John Doe', email: 'john@example.com', createdAt: new java.util.Date().toString(), metadata: { version: '1.0', source: 'mock' } } : { error: { code: 'NOT_FOUND', message: 'User not found' } }
  * def responseStatus = id == '1' ? 200 : 404

Scenario: pathMatches('/users/1/profile') && methodIs('get')
  * def response = { id: 1, profile: { nickname: 'jdoe', preferences: {} } }
  * def responseStatus = 200

# Order service
Scenario: pathMatches('/orders') && methodIs('post')
  * def response = { id: 1, status: 'CREATED', userId: request.userId, sku: request.sku, quantity: request.quantity }
  * def responseStatus = 201

Scenario: pathMatches('/orders/{id}') && methodIs('get')
  * def id = pathParams.id
  * def response = id == '1' ? { id: 1, status: 'CREATED', userId: 1, sku: 'ABC-123', quantity: 1 } : { error: { code: 'NOT_FOUND', message: 'Order not found' } }
  * def responseStatus = id == '1' ? 200 : 404

# External dependencies examples
Scenario: pathMatches('/api/external/users/{id}') && methodIs('get')
  * def body = read('classpath:mocks/mock-responses/user-responses.json')
  * def response = body
  * def responseStatus = 200

Scenario: pathMatches('/api/external/payments') && methodIs('post')
  * def response = { transactionId: '#(uuid())', status: 'SUCCESS' }
  * def responseStatus = 200