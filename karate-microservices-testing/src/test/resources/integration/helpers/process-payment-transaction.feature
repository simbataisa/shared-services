Feature: Helpers - Process Payment Transaction

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    # Allow injected auth / headers; else fallback to login + common headers
    * def providedAuth = karate.get('auth')
    * def login = providedAuth ? null : karate.callSingle('classpath:common/auth/login.feature')
    * def auth = providedAuth ? providedAuth : { token: login.token }
    * def providedHeaders = karate.get('headers')
    * configure headers = providedHeaders ? providedHeaders : read('classpath:common/headers/common-headers.js')

    @name=processPaymentTransaction
  Scenario: Process payment transaction with provided overrides
    # Gather overrides so the JSON template evaluates in this scope
    * def paymentToken = karate.get('paymentToken', '')
    * def paymentMethod = karate.get('paymentMethod', 'CREDIT_CARD')
    * def gatewayName = karate.get('gatewayName', 'STRIPE')
    * def paymentMethodDetails = karate.get('paymentMethodDetails', { cardNumber: '4111111111111111', expMonth: 12, expYear: 2030, cvv: '123' })
    * def metadata = karate.get('metadata', { source: 'karate-e2e' })

    * def body = read('classpath:integration/helpers/data/request/process-payment-transaction.json')
    Given path '/api/v1/payments/transactions/process'
    And request body
    * print 'Process transaction payload:', body
    When method post
    * print 'Process transaction response:', response
    Then status 201
    And match response.data.transactionStatus == 'SUCCESS'
    * def result = { response: response }