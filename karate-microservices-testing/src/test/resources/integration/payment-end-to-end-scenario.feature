Feature: Reusable Payment E2E Scenario

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def utils = karate.get('utils')
    * configure retry = { count: 3, interval: 1000 }

    # Get injected parameters
    * def testCase = karate.get('testCase')
    * def paymentMethod = karate.get('paymentMethod')
    * def gateway = karate.get('gateway')
    * def allowedPaymentMethods = karate.get('allowedPaymentMethods')
    * def allowedGateways = karate.get('allowedGateways')
    * def paymentMethodDetails = karate.get('paymentMethodDetails')
    * def expectedStatus = karate.get('expectedStatus')
    * def description = karate.get('description')
    * def auth = karate.get('auth')
    * def headers = karate.get('headers')
    * print 'ProvidedHeaders:', headers
    * print 'paymentMethod:', paymentMethod
    * print 'gateway:', gateway
    * print 'allowedGateways:', allowedGateways
    * print 'paymentMethodDetails:', paymentMethodDetails
    * print 'expectedStatus:', expectedStatus
    * print 'description:', description 
    * print 'auth:', auth
    * print 'headers:', headers


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
    And retry until response.data.status == expectedStatus
    When method get
    Then status 200
    * print 'Payment request details:', response
    And match response.data.status == expectedStatus
    And match response.data.paidAt != null
    And match response.data.id == paymentRequestId
    * print '✓ Payment e2e verified: request', paymentRequestId, 'transaction', transactionId
    * print '========================================='
