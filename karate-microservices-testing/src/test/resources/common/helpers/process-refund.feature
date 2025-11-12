Feature: Helpers - Process Refund

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    # Allow injected auth / headers; else fallback to login + common headers
    * def providedAuth = karate.get('auth')
    * def providedHeaders = karate.get('headers')
    * def hasValidAuth = providedAuth && providedAuth.token && (providedAuth.token + '') != ''
    * def hasValidHeaders = providedHeaders && providedHeaders.Authorization && (providedHeaders.Authorization + '').startsWith('Bearer ')
    * def login = (!hasValidAuth || !hasValidHeaders) ? karate.callSingle('classpath:common/auth/login.feature') : null
    * def auth = hasValidAuth ? providedAuth : { token: login.token }
    * configure headers = (hasValidAuth && hasValidHeaders) ? providedHeaders : read('classpath:common/headers/common-headers.js')

  @name=processRefund
  Scenario: Process refund (requires refundId)
    * def refundId = karate.get('refundId')
    Given path '/api/v1/payments/refunds/' + refundId + '/process'
    When method post
    * print 'Process refund response:', response
    Then status 200
    And match response.data.refundStatus == 'SUCCESS'
    * def result = { response: response }