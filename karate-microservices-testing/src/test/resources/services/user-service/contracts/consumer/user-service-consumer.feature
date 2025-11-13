Feature: Consumer contract for user payload

  Background:
    * url karate.get('mockServerUrl', 'http://localhost:8090')
    * configure retry = { count: 20, interval: 1000 }

    @contract
  Scenario: Expected shape for /users/1
    Given path '/users/1'
    When method get
    Then status 200
    And match response ==
    """
    { id: '#number', name: '#string', email: '#string',
    createdAt: '#string', metadata: { version: '#string', source: '#string' } }
    """
    * karate.write(response, 'target/contracts/user-consumer-contract.json')