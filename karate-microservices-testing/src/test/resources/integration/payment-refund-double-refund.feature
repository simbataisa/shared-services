Feature: Attempt double refund after successful full refund

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')
    * def headersFn = read('classpath:common/headers/common-headers.js')
    * def headersPreview = headersFn()
    * print 'Computed headers preview:', headersPreview

    @e2e @payments @refund @failure @business-rule @double-refund
  Scenario: Attempt second full refund on same transaction after successful refund
    # Re-use the full success flow to set up a fully refunded transaction
    * def success = call read('classpath:integration/payment-refund-flow-success.feature')
    * def paymentRequestId = success.paymentRequestId
    * def transactionId = success.transactionId
    * def originalAmount = success.originalAmount
    * def originalCurrency = success.originalCurrency
    * print 'Setup from success flow:', { paymentRequestId: paymentRequestId, transactionId: transactionId, amount: originalAmount, currency: originalCurrency }

    # Second refund attempt - should fail (already refunded / cannot be refunded)
    Given path '/api/v1/payments/refunds'
    And request { paymentTransactionId: '#(transactionId)', refundAmount: '#(originalAmount)', currency: '#(originalCurrency)', reason: 'Second full refund attempt', gatewayName: 'Stripe' }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 400
    * print 'Second refund error response:', response
    And match response.message contains 'exceeds remaining refundable amount'