Feature: Payments API

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')

  Scenario: List payment requests
    Given path '/api/v1/payments/requests'
    When method get
    Then status 200
    And match response.success == true

  Scenario: List payment transactions
    Given path '/api/v1/payments/transactions'
    When method get
    Then status 200
    And match response.success == true

  Scenario: List payment refunds
    Given path '/api/v1/payments/refunds'
    When method get
    Then status 200
    And match response.success == true

  Scenario: List payment audit logs
    Given path '/api/v1/payments/audit-logs'
    When method get
    Then status 200
    And match response.success == true

  Scenario: Payment request stats
    Given path '/api/v1/payments/stats/requests'
    When method get
    Then status 200
    And match response.success == true

  Scenario: Payment transaction stats
    Given path '/api/v1/payments/stats/transactions'
    When method get
    Then status 200
    And match response.success == true

  Scenario: Payment refund stats
    Given path '/api/v1/payments/stats/refunds'
    When method get
    Then status 200
    And match response.success == true