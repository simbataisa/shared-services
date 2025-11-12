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
    When method post
    * print 'Validation error response:', response
    Then status 400
    And match response.message contains 'Payment transaction ID is required'

    @e2e @payments @refund @failure @validation
  Scenario: Attempt refund with missing refund amount
    # Create a valid payment first
    * def allowedPaymentMethods = ['STRIPE']
    * def preSelectedPaymentMethod = 'CREDIT_CARD'
    * def paymentGateway = 'Stripe'
    * def createResult = call read('classpath:common/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentToken = createResult.response.data.paymentToken
    * def processResult = call read('classpath:common/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
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
    * def createResult = call read('classpath:common/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentToken = createResult.response.data.paymentToken
    * def processResult = call read('classpath:common/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
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
    * def createResult = call read('classpath:common/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentToken = createResult.response.data.paymentToken
    * def originalAmount = createResult.response.data.amount
    * def originalCurrency = createResult.response.data.currency || 'USD'
    * def processResult = call read('classpath:common/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'CREDIT_CARD', gatewayName: 'Stripe', paymentMethodDetails: { stripeToken: 'tok_visa' }, auth: #(auth), headers: #(headersPreview) }
    * def transactionId = processResult.response.data.id

    # Try to refund more than the original amount
    * def excessiveAmount = originalAmount * 2
    Given path '/api/v1/payments/refunds'
    * def requestPayload = { paymentTransactionId: '#(transactionId)', refundAmount: '#(excessiveAmount)', currency: '#(originalCurrency)', reason: 'Test excessive refund', gatewayName: 'Stripe' }
    * print 'Request payload:', requestPayload
    And request requestPayload
    When method post
    Then status 400
    * print 'Business rule error response:', response
    And match response.message contains 'exceeds'

    @e2e @payments @refund @failure @gateway
  Scenario: Create payment (Bank Transfer), process payment, attempt refund and verify refund FAILED
    # Create a payment request via helper using Bank Transfer
    * def allowedPaymentMethods = ['BANK_TRANSFER']
    * def preSelectedPaymentMethod = 'BANK_TRANSFER'
    * def paymentGateway = 'BankTransfer'
    * def createResult = call read('classpath:common/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethods), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentRequestId = createResult.response.data.id
    * def paymentToken = createResult.response.data.paymentToken
    * def originalAmount = createResult.response.data.amount
    * def originalCurrency = createResult.response.data.currency || 'USD'
    * print 'Created BT payment request:', paymentRequestId, 'token:', paymentToken, 'amount:', originalAmount, 'currency:', originalCurrency

    # Process a payment transaction for the request via Bank Transfer
    * def btDetails = { accountNumber: '12345678', routingNumber: '021000021', accountHolderName: 'Jane Doe', bankName: 'Test Bank' }
    * def processResult = call read('classpath:common/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: 'BANK_TRANSFER', gatewayName: 'BankTransfer', paymentMethodDetails: #(btDetails), auth: #(auth), headers: #(headersPreview) }
    * print 'Bank Transfer transaction processed:', processResult.response
    And match processResult.response.data.transactionStatus == 'SUCCESS'
    And match processResult.response.data.paymentRequestId == paymentRequestId
    * def transactionId = processResult.response.data.id || processResult.response.id
    * print 'BT Transaction id:', transactionId

    # Create a refund request (this will be created, but gateway processing should FAIL)
    Given path '/api/v1/payments/refunds'
    * def refundReqPayload = { paymentTransactionId: '#(transactionId)', refundAmount: '#(originalAmount)', currency: '#(originalCurrency)', reason: 'Test refund failure (BT not refundable)', gatewayName: 'BankTransfer', metadata: { source: 'karate-e2e', note: 'Failed refund test' } }
    And request refundReqPayload
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    * print 'Refund request payload (BT):', refundReqPayload
    When method post
    * print 'Create refund response (BT):', response
    Then status 201
    * def refundId = response.data.id
    * print 'Created refund ID (BT):', refundId

    # Process the refund through the Bank Transfer gateway - expect FAILED
    Given path '/api/v1/payments/refunds/' + refundId + '/process'
    And headers headersPreview
    And header Authorization = 'Bearer ' + auth.token
    When method post
    * print 'Process refund response (BT):', response
    Then status 200
    And match response.data.refundStatus == 'FAILED'
    * print 'Refund FAILED as expected for Bank Transfer', response.data
