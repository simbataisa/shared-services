Feature: End-to-end payment success flow

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')
    * def headersFn = read('classpath:common/headers/common-headers.js')
    * def headersPreview = headersFn()
    * print 'Computed headers preview:', headersPreview
    * def utils = karate.get('utils')
    * configure retry = { count: 20, interval: 1000 }


    @e2e @payments @success
  Scenario: Create payment request, process payment, and verify request marked as COMPLETED
    # Create a payment request via helper
    * def allowedPaymentMethods = ['STRIPE']
    * def preSelectedPaymentMethod = 'CREDIT_CARD'
    * def paymentGateway = 'Stripe'
    * def createResult = call read('classpath:common/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentRequestId = createResult.response.data.id
    * def paymentToken = createResult.response.data.paymentToken
    * print 'createResult', createResult
    * print 'Created payment request:', paymentRequestId, 'with token:', paymentToken

    # Process a payment transaction for the request
    * def processResult = call read('classpath:common/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
    * print 'Payment transaction processed:', processResult.response
    And match processResult.response.data.transactionStatus == 'SUCCESS'
    And match processResult.response.data.paymentRequestId == paymentRequestId
    * def transactionId = processResult.response.id

    # Verify the payment request is marked as COMPLETED
    Given path '/api/v1/payments/requests/' + paymentRequestId
    * print 'Verifying payment request:', paymentRequestId
    And retry until response.data.status == 'COMPLETED'
    When method get
    Then status 200
    * print 'Payment request details:', response
    And match response.data.status == 'COMPLETED'
    And match response.data.paidAt != null
    And match response.data.id == paymentRequestId
    * print 'Payment e2e verified: request', paymentRequestId, 'transaction', transactionId
