Feature: Helpers - Create Payment Request

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    # Allow injected auth / headers; else fallback to login + common headers
    * def providedAuth = karate.get('auth')
    * print 'ProvidedAuth:', providedAuth
    * def login = providedAuth ? null : karate.callSingle('classpath:common/auth/login.feature')
    * def auth = providedAuth ? providedAuth : { token: login.token }
    * def providedHeaders = karate.get('headers')
    * print 'ProvidedHeaders:', providedHeaders
    * configure headers = providedHeaders ? providedHeaders : read('classpath:common/headers/common-headers.js')
    * def utils = karate.get('utils')

    @name=createPaymentRequest
  Scenario: Create payment request with provided payload
    * def body = read('classpath:integration/helpers/data/request/create-payment-request.json')
    Given path '/api/v1/payments/requests'
    And request body
    * print 'Request body:', body
    When method post
    * print 'Response:', response
    Then status 201
    And match response.id != null
    And match response.paymentToken != null
    * def id = response.id
    * def paymentToken = response.paymentToken