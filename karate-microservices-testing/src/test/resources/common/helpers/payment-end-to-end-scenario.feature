Feature: Reusable Payment E2E Scenario

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def utils = karate.get('utils')
    * configure retry = { count: 20, interval: 1000 }
  
    # Get injected parameters
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
    * def testCase = karate.get('testCase')
    * def paymentMethod = karate.get('paymentMethod')
    * def gateway = karate.get('gateway')
    * def allowedPaymentMethods = karate.get('allowedPaymentMethods')
    * def allowedGateways = karate.get('allowedGateways')
    * def paymentMethodDetails = karate.get('paymentMethodDetails')
    * def expectedStatus = karate.get('expectedStatus')
    * def expectedStatus = (!expectedStatus || expectedStatus == 'COMPLETED') ? 'COMPLETED' : expectedStatus
    * def description = karate.get('description')
    
    @reusable
  Scenario: Execute single payment scenario
    * print '========================================='
    * print 'Test Case:', testCase
    * print 'Description:', description
    * print 'Payment Method:', paymentMethod
    * print 'Gateway:', gateway
    * print '========================================='

    # Create a payment request via helper
    * def createResult = call read('classpath:common/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(paymentMethod), paymentGateway: #(gateway), auth: #(auth), headers: #(headers) }
    * def paymentRequestId = createResult.response.data.id
    * def paymentToken = createResult.response.data.paymentToken
    * print 'Created payment request:', paymentRequestId, 'with token:', paymentToken

    # Process a payment transaction for the request
    * def processResult = call read('classpath:common/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: #(paymentMethod), gatewayName: #(gateway), paymentMethodDetails: #(paymentMethodDetails), auth: #(auth), headers: #(headers) }
    * print 'Payment transaction processed:', processResult.response
    And match processResult.response.data.transactionStatus == 'SUCCESS'
    And match processResult.response.data.paymentRequestId == paymentRequestId
    * def transactionId = processResult.response.id

    # Verify the payment request is marked as expected status
    Given path '/api/v1/payments/requests/' + paymentRequestId
    * print 'Verifying payment request:', paymentRequestId
    * print 'Expected status:', expectedStatus
    And retry until response.data.status == expectedStatus
    When method get
    * print 'Payment request details:', response
    Then status 200
    And match response.data.status == expectedStatus
    And match response.data.paidAt != null
    And match response.data.id == paymentRequestId
    * print '✓ Payment e2e verified: request', paymentRequestId, 'transaction', transactionId
    * print '========================================='
