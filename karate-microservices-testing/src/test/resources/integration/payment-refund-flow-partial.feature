Feature: End-to-end payment partial refund flow

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')
    * def headersFn = read('classpath:common/headers/common-headers.js')
    * def headersPreview = headersFn()
    * print 'Computed headers preview:', headersPreview
    * configure retry = { count: 3, interval: 1000 }

    @e2e @payments @refund @partial @success
  Scenario: Create payment, process payment, refund partial amount, and verify request marked as PARTIAL_REFUND
    # Create a payment request via helper
    * def allowedPaymentMethods = ['STRIPE']
    * def preSelectedPaymentMethod = 'CREDIT_CARD'
    * def paymentGateway = 'Stripe'
    * def createResult = call read('classpath:integration/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentRequestId = createResult.response.data.id
    * def paymentToken = createResult.response.data.paymentToken
    * def originalAmount = createResult.response.data.amount
    * def originalCurrency = createResult.response.data.currency || 'USD'
    * print 'Created payment request:', paymentRequestId, 'token:', paymentToken, 'amount:', originalAmount, 'currency:', originalCurrency

    # Process a payment transaction for the request
    * def processResult = call read('classpath:integration/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
    * print 'Payment transaction processed:', processResult.response
    And match processResult.response.data.transactionStatus == 'SUCCESS'
    And match processResult.response.data.paymentRequestId == paymentRequestId
    * def transactionId = processResult.response.data.id || processResult.response.id
    * print 'Transaction id:', transactionId

    # Calculate partial refund amount (50% of original)
    * def partialRefundAmount = originalAmount / 2
    * print 'Partial refund amount:', partialRefundAmount, 'of original:', originalAmount

    # Initiate a partial refund on the payment transaction
    Given path '/api/v1/payments/refunds'
    And request { paymentTransactionId: '#(transactionId)', refundAmount: '#(partialRefundAmount)', currency: '#(originalCurrency)', reason: 'Partial refund requested by customer', gatewayName: 'Stripe', metadata: { source: 'karate-e2e', note: 'Partial refund test - 50%' } }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 201
    * print 'Partial refund response:', response

    # Verify the payment request is marked as PARTIAL_REFUND
    Given path '/api/v1/payments/requests/' + paymentRequestId
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    And retry until response.data && response.data.status == 'PARTIAL_REFUND'
    When method get
    Then status 200
    * print 'Payment request after partial refund:', response
    And match response.data.status == 'PARTIAL_REFUND'
    And match response.data.id == paymentRequestId
    And match response.data.amount == originalAmount
    And match response.data.currency == originalCurrency
    * print 'Payment partial refund e2e verified: request', paymentRequestId, 'transaction', transactionId, 'partial amount:', partialRefundAmount
