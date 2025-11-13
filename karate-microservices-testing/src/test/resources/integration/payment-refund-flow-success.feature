Feature: End-to-end payment full refund flow

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

    @e2e @payments @refund @success
  Scenario: Create payment, process payment, refund full amount, and verify request marked as REFUNDED
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

    # Create a refund request via helper
    * def createRefundResult = call read('classpath:common/helpers/create-refund-request.feature') { paymentTransactionId: #(transactionId), refundAmount: #(originalAmount), currency: #(originalCurrency), reason: 'Customer requested refund', gatewayName: 'Stripe', metadata: { source: 'karate-e2e', note: 'Full refund test' }, auth: #(auth), headers: #(headersPreview) }
    * def refundId = createRefundResult.refundId
    * print 'Created refund ID:', refundId

    # Process the refund through the payment gateway via helper
    * def processRefundResult = call read('classpath:common/helpers/process-refund.feature') { refundId: #(refundId), auth: #(auth), headers: #(headersPreview) }
    * print 'Process refund response:', processRefundResult.response
    And match processRefundResult.response.data.refundStatus == 'SUCCESS'

    # Verify the payment request is marked as REFUNDED
    Given path '/api/v1/payments/requests/' + paymentRequestId
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    And retry until response.data && response.data.status == 'REFUNDED'
    When method get
    Then status 200
    * print 'Payment request after refund:', response
    And match response.data.status == 'REFUNDED'
    And match response.data.id == paymentRequestId
    And match response.data.amount == originalAmount
    And match response.data.currency == originalCurrency
    * print 'Payment refund e2e verified: request', paymentRequestId, 'transaction', transactionId

    # Expose setup values for re-use by calling features
    * def result = { paymentRequestId: #(paymentRequestId), transactionId: #(transactionId), originalAmount: #(originalAmount), originalCurrency: #(originalCurrency), refundId: #(refundId) }