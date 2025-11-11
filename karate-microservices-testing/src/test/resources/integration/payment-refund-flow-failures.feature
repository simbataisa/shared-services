Feature: Payment refund flow failure scenarios

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')
    * def headersFn = read('classpath:common/headers/common-headers.js')
    * def headersPreview = headersFn()
    * print 'Computed headers preview:', headersPreview

  @e2e @payments @refund @failure @validation
  Scenario: Attempt refund with missing required fields
    # Try to create a refund without paymentTransactionId
    Given path '/api/v1/payments/refunds'
    And request { refundAmount: 100.00, currency: 'USD', reason: 'Test refund' }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 400
    * print 'Validation error response:', response
    And match response.message contains 'Payment transaction ID is required'

  @e2e @payments @refund @failure @validation
  Scenario: Attempt refund with missing refund amount
    # Create a valid payment first
    * def allowedPaymentMethods = ['STRIPE']
    * def preSelectedPaymentMethod = 'CREDIT_CARD'
    * def paymentGateway = 'Stripe'
    * def createResult = call read('classpath:integration/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentToken = createResult.response.data.paymentToken
    * def processResult = call read('classpath:integration/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
    * def transactionId = processResult.response.data.id

    # Try to create a refund without refundAmount
    Given path '/api/v1/payments/refunds'
    And request { paymentTransactionId: '#(transactionId)', currency: 'USD', reason: 'Test refund' }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 400
    * print 'Validation error response:', response
    And match response.message contains 'Refund amount is required'

  @e2e @payments @refund @failure @validation
  Scenario: Attempt refund with invalid currency format
    # Create a valid payment first
    * def allowedPaymentMethods = ['STRIPE']
    * def preSelectedPaymentMethod = 'CREDIT_CARD'
    * def paymentGateway = 'Stripe'
    * def createResult = call read('classpath:integration/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentToken = createResult.response.data.paymentToken
    * def processResult = call read('classpath:integration/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
    * def transactionId = processResult.response.data.id

    # Try to create a refund with invalid currency (not 3 characters)
    Given path '/api/v1/payments/refunds'
    And request { paymentTransactionId: '#(transactionId)', refundAmount: 50.00, currency: 'US', reason: 'Test refund' }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 400
    * print 'Validation error response:', response
    And match response.message contains 'Currency must be exactly 3 characters'

  @e2e @payments @refund @failure @business-rule
  Scenario: Attempt refund on non-existent transaction
    # Generate a random UUID for a non-existent transaction
    * def nonExistentId = 'a0a0a0a0-b0b0-c0c0-d0d0-e0e0e0e0e0e0'

    # Try to create a refund for non-existent transaction
    Given path '/api/v1/payments/refunds'
    And request { paymentTransactionId: '#(nonExistentId)', refundAmount: 100.00, currency: 'USD', reason: 'Test refund', gatewayName: 'Stripe' }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 400
    * print 'Business rule error response:', response
    And match response.message contains 'not found'

  @e2e @payments @refund @failure @business-rule
  Scenario: Attempt to refund more than the original payment amount
    # Create a valid payment first
    * def allowedPaymentMethods = ['STRIPE']
    * def preSelectedPaymentMethod = 'CREDIT_CARD'
    * def paymentGateway = 'Stripe'
    * def createResult = call read('classpath:integration/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentToken = createResult.response.data.paymentToken
    * def originalAmount = createResult.response.data.amount
    * def originalCurrency = createResult.response.data.currency || 'USD'
    * def processResult = call read('classpath:integration/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
    * def transactionId = processResult.response.data.id

    # Try to refund more than the original amount
    * def excessiveAmount = originalAmount * 2
    Given path '/api/v1/payments/refunds'
    And request { paymentTransactionId: '#(transactionId)', refundAmount: '#(excessiveAmount)', currency: '#(originalCurrency)', reason: 'Test excessive refund', gatewayName: 'Stripe' }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 400
    * print 'Business rule error response:', response
    And match response.message contains 'exceeds' || response.message contains 'more than'

  @e2e @payments @refund @failure @business-rule
  Scenario: Attempt double full refund on same transaction
    # Create a valid payment first
    * def allowedPaymentMethods = ['STRIPE']
    * def preSelectedPaymentMethod = 'CREDIT_CARD'
    * def paymentGateway = 'Stripe'
    * def createResult = call read('classpath:integration/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentRequestId = createResult.response.data.id
    * def paymentToken = createResult.response.data.paymentToken
    * def originalAmount = createResult.response.data.amount
    * def originalCurrency = createResult.response.data.currency || 'USD'
    * def processResult = call read('classpath:integration/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
    * def transactionId = processResult.response.data.id

    # First refund - should succeed
    Given path '/api/v1/payments/refunds'
    And request { paymentTransactionId: '#(transactionId)', refundAmount: '#(originalAmount)', currency: '#(originalCurrency)', reason: 'First full refund', gatewayName: 'Stripe' }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 201
    * print 'First refund response:', response

    # Wait for payment request status to update
    * configure retry = { count: 5, interval: 1000 }
    Given path '/api/v1/payments/requests/' + paymentRequestId
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    And retry until response.data && response.data.status == 'REFUNDED'
    When method get
    Then status 200

    # Second refund attempt - should fail
    Given path '/api/v1/payments/refunds'
    And request { paymentTransactionId: '#(transactionId)', refundAmount: '#(originalAmount)', currency: '#(originalCurrency)', reason: 'Second full refund attempt', gatewayName: 'Stripe' }
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    Then status 400
    * print 'Second refund error response:', response
    And match response.message contains 'already refunded' || response.message contains 'cannot be refunded'
