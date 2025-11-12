Feature: Helpers - Create Refund Request

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

  @name=createRefundRequest
  Scenario: Create refund request (supports overrides)
    * def paymentTransactionId = karate.get('paymentTransactionId')
    * def refundAmount = karate.get('refundAmount')
    * def currency = karate.get('currency', 'USD')
    * def reason = karate.get('reason', 'Customer requested refund')
    * def gatewayName = karate.get('gatewayName', 'Stripe')
    * def metadata = karate.get('metadata', { source: 'karate-e2e' })

    * def payload = { paymentTransactionId: #(paymentTransactionId), refundAmount: #(refundAmount), currency: #(currency), reason: #(reason), gatewayName: #(gatewayName), metadata: #(metadata) }
    Given path '/api/v1/payments/refunds'
    And request payload
    * print 'Refund request payload:', payload
    When method post
    * print 'Create refund response:', response
    Then status 201
    * def refundId = response.data.id
    * def result = { refundId: refundId, response: response }