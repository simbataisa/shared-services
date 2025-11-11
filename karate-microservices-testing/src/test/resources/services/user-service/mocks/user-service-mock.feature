Feature: User Service Mock

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