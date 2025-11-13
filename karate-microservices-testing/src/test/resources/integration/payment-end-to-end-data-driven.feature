Feature: End-to-end payment success flow - Data Driven

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

    @e2e @payments @success @data-driven
  Scenario Outline: <testCase> - Create payment request with <paymentMethod> via <gateway>
    * print '========================================='
    * print 'Test Case: <testCase>'
    * print 'Payment Method: <paymentMethod>'
    * print 'Gateway: <gateway>'
    * print '========================================='

    # Parse payment method details from JSON string
    * def paymentMethodDetailsObj = karate.jsonPath(paymentMethodDetails, '$')
    * print 'Payment Method Details:', paymentMethodDetailsObj

    # Parse allowedPaymentMethods array
    * def allowedPaymentMethodsArray = karate.jsonPath(allowedPaymentMethods, '$')
    * print 'Allowed Payment Methods:', allowedPaymentMethodsArray

    # Create a payment request via helper
    * def preSelectedPaymentMethod = '<paymentMethod>'
    * def paymentGateway = '<gateway>'
    * def createResult = call read('classpath:common/helpers/create-payment-request.feature') { allowedPaymentMethods: #(allowedPaymentMethodsArray), preSelectedPaymentMethod: #(preSelectedPaymentMethod), paymentGateway: #(paymentGateway), auth: #(auth), headers: #(headersPreview) }
    * def paymentRequestId = createResult.response.data.id
    * def paymentToken = createResult.response.data.paymentToken
    * print 'Created payment request:', paymentRequestId, 'with token:', paymentToken

    # Process a payment transaction for the request
    * def processResult = call read('classpath:common/helpers/process-payment-transaction.feature') { paymentToken: #(paymentToken), paymentMethod: '<paymentMethod>', gatewayName: '<gateway>', paymentMethodDetails: #(paymentMethodDetailsObj), auth: #(auth), headers: #(headersPreview) }
    * print 'Payment transaction processed:', processResult.response
    And match processResult.response.data.transactionStatus == 'SUCCESS'
    And match processResult.response.data.paymentRequestId == paymentRequestId
    * def transactionId = processResult.response.id

    # Verify the payment request is marked as COMPLETED
    Given path '/api/v1/payments/requests/' + paymentRequestId
    * print 'Verifying payment request:', paymentRequestId
    And retry until response.data.status == '<expectedStatus>'
    When method get
    Then status 200
    * print 'Payment request details:', response
    And match response.data.status == '<expectedStatus>'
    And match response.data.paidAt != null
    And match response.data.id == paymentRequestId
    * print 'Payment e2e verified: request', paymentRequestId, 'transaction', transactionId

    Examples:
      | testCase              | paymentMethod | gateway       | allowedPaymentMethods | paymentMethodDetails                                                                                    | expectedStatus | description                                    |
      | stripe_credit_card    | CREDIT_CARD   | Stripe        | ["STRIPE"]            | {"stripeToken":"tok_visa"}                                                                              | COMPLETED      | Stripe credit card payment with valid token    |
      | paypal_credit_card    | CREDIT_CARD   | PayPal        | ["PAYPAL"]            | {"paypalOrderId":"ORDER123"}                                                                            | COMPLETED      | PayPal credit card payment with order ID       |
      | bank_transfer         | BANK_TRANSFER | BankTransfer  | ["BANK_TRANSFER"]     | {"accountNumber":"1234567890","routingNumber":"987654321","accountHolderName":"John Doe"}              | COMPLETED      | Bank transfer payment with account details     |
