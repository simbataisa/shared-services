Feature: Helpers - Create Payment Request

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
    * def utils = karate.get('utils')

    @name=createPaymentRequest
  Scenario: Create payment request with provided payload
    # Gather overrides (if any) so the JSON template evaluates in this scope
    * def title = karate.get('title', null)
    * def amount = karate.get('amount', null)
    * def currency = karate.get('currency', null)
    * def payerName = karate.get('payerName', null)
    * def payerEmail = karate.get('payerEmail', null)
    * def allowedPaymentMethods = karate.get('allowedPaymentMethods', ['CREDIT_CARD'])
    * def preSelectedPaymentMethod = karate.get('preSelectedPaymentMethod', 'CREDIT_CARD')
    * def paymentGateway = karate.get('paymentGateway', 'Stripe')
    * def tenantId = karate.get('tenantId', 1)
    * def metadata = karate.get('metadata', { source: 'karate-e2e' })
    * def body = read('classpath:common/helpers/data/request/create-payment-request.json')
    Given path '/api/v1/payments/requests'
    And request body
    * print 'Request body:', body
    When method post
    * print 'Response:', response
    Then status 201
    # Normalize response shape to support backend (data wrapper) and mock (flat) responses
    * def respData = response.data ? response.data : response
    And match respData.id != null
    And match respData.paymentToken != null
    * def id = respData.id
    * def paymentToken = respData.paymentToken