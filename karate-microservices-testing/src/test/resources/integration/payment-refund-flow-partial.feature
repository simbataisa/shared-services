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
    * configure retry = { count: 20, interval: 1000 }

    @e2e @payments @refund @partial @success
  Scenario: Create payment, process payment, refund partial amount, and verify request marked as PARTIAL_REFUND
    # Create a payment request via helper
    * def allowedPaymentMethods = ['STRIPE']
    * def preSelectedPaymentMethod = 'CREDIT_CARD'
    * def paymentGateway = 'Stripe'
    * def createResult = call read('classpath:common/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentRequestId = createResult.response.data.id
    * def paymentToken = createResult.response.data.paymentToken
    * def originalAmount = createResult.response.data.amount
    * def originalCurrency = createResult.response.data.currency || 'USD'
    * print 'Created payment request:', paymentRequestId, 'token:', paymentToken, 'amount:', originalAmount, 'currency:', originalCurrency

    # Process a payment transaction for the request
    * def processResult = call read('classpath:common/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
    * print 'Payment transaction processed:', processResult.response
    And match processResult.response.data.transactionStatus == 'SUCCESS'
    And match processResult.response.data.paymentRequestId == paymentRequestId
    * def transactionId = processResult.response.data.id || processResult.response.id
    * print 'Transaction id:', transactionId

    # Calculate partial refund amount (50% of original)
    * def partialRefundAmount = Math.round((originalAmount / 2) * 100) / 100
    * print 'Partial refund amount:', partialRefundAmount, 'of original:', originalAmount

    # Initiate a partial refund using helper
    * def createRefundResult = call read('classpath:common/helpers/create-refund-request.feature') { paymentTransactionId: #(transactionId), refundAmount: #(partialRefundAmount), currency: #(originalCurrency), reason: 'Partial refund requested by customer', gatewayName: 'Stripe', metadata: { source: 'karate-e2e', note: 'Partial refund test - 50%' }, auth: #(auth), headers: #(headersPreview) }
    * def refundId = createRefundResult.refundId
    * print 'Partial refund created:', createRefundResult.response

    # Process the partial refund using helper
    * def processRefundResult = call read('classpath:common/helpers/process-refund.feature') { refundId: #(refundId), auth: #(auth), headers: #(headersPreview) }
    * print 'Partial refund processed:', processRefundResult.response
    And match processRefundResult.response.data.refundStatus == 'SUCCESS'
    
    # Verify the payment request is marked as PARTIAL_REFUND
    Given path '/api/v1/payments/requests/' + paymentRequestId
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    # And retry until response.data && response.data.status == 'PARTIAL_REFUND'
    When method get
    * print 'Payment request after partial refund:', response
    Then status 200
    And match response.data.status == 'PARTIAL_REFUND'
    And match response.data.id == paymentRequestId
    And match response.data.amount == originalAmount
    And match response.data.currency == originalCurrency
    * print 'Payment partial refund e2e verified: request', paymentRequestId, 'transaction', transactionId, 'partial amount:', partialRefundAmount
