Feature: Consumer contract for order payload

Background:
  * url karate.get('mockServerUrl', 'http://localhost:8090')

@contract
Scenario: Expected shape for /orders/1
  Given path '/orders/1'
  When method get
  Then status 200
  And match response ==
  """
  { id: '#number', status: '#string', userId: '#number', sku: '#string', quantity: '#number' }
  """