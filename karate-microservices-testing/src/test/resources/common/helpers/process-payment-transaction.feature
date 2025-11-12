Feature: Helpers - Process Payment Transaction
    This requires a payment request to be created first: paymentToken
  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    # Allow injected auth / headers; else fallback to login + common headers
    * def providedAuth = karate.get('auth')
    * def providedHeaders = karate.get('headers')
    * def hasValidAuth = providedAuth && providedAuth.token && (providedAuth.token + '') != ''
    * def hasValidHeaders = providedHeaders && providedHeaders.Authorization && (providedHeaders.Authorization + '').startsWith('Bearer ')
    * print 'providedAuth valid:', hasValidAuth, 'providedHeaders valid:', hasValidHeaders
    * def login = (!hasValidAuth || !hasValidHeaders) ? karate.callSingle('classpath:common/auth/login.feature') : null
    * def auth = hasValidAuth ? providedAuth : { token: login.token }
    * configure headers = (hasValidAuth && hasValidHeaders) ? providedHeaders : read('classpath:common/headers/common-headers.js')

    @name=processPaymentTransaction
  Scenario: Process payment transaction with provided overrides
    # Gather overrides so the JSON template evaluates in this scope
    * def paymentToken = karate.get('paymentToken', '')
    * def paymentMethod = karate.get('paymentMethod', 'CREDIT_CARD')
    * def gatewayName = karate.get('gatewayName', 'STRIPE')
    * def paymentMethodDetails = karate.get('paymentMethodDetails', { cardNumber: '4111111111111111', expMonth: 12, expYear: 2030, cvv: '123' })
    * def metadata = karate.get('metadata', { source: 'karate-e2e' })

    * def body = read('classpath:common/helpers/data/request/process-payment-transaction.json')
    * def isMissingToken = !body.paymentToken || (body.paymentToken + '') == ''
    Given path '/api/v1/payments/transactions/process'
    And request body
    * print 'Process transaction payload:', body
    When method post
    * print 'Process transaction response:', response
    * def expectedStatus = isMissingToken ? 400 : 201
    Then match responseStatus == expectedStatus
    And assert isMissingToken ? (response.message == 'Payment token is required') : (response.success == true)
    * def result = { response: response }