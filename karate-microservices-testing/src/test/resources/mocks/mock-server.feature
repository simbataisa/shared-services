Feature: Local mock for external deps and services

Background:
  * def uuid = function(){ return java.util.UUID.randomUUID() + '' }
  * def payments = {}
  * def transactions = {}
  * def stripeState = { tokens: {}, charges: {} }
  * def paypalState = { orders: {}, captures: {} }
  * def bankTransferState = { transfers: {} }

  # Payment gateway state is managed locally and delegated via tagged calls

# ==== Health Check ====
Scenario: pathMatches('/stripe/health') && methodIs('get')
  * def response = { status: 'UP', message: 'Mock server is running', timestamp: new java.util.Date().toString() }
  * def responseStatus = 200

Scenario: pathMatches('/paypal/health') && methodIs('get')
  * def response = { status: 'UP', message: 'Mock server is running', timestamp: new java.util.Date().toString() }
  * def responseStatus = 200

Scenario: pathMatches('/bank-transfer/health') && methodIs('get')
  * def response = { status: 'UP', message: 'Mock server is running', timestamp: new java.util.Date().toString() }
  * def responseStatus = 200

Scenario: pathMatches('/health') && methodIs('get')
  * def response = { status: 'UP', message: 'Mock server is running', timestamp: new java.util.Date().toString() }
  * def responseStatus = 200

# ==== Delegate to gateway mock features ====

# Stripe gateway - explicit delegates
Scenario: pathMatches('/stripe/v1/tokens') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/stripe-mock.feature@stripe_tokens_post') { tokens: stripeState.tokens, charges: stripeState.charges }
  * if (result && result.tokens) karate.set('stripeState.tokens', result.tokens)
  * if (result && result.charges) karate.set('stripeState.charges', result.charges)
  * def response = result.response
  * def responseStatus = result.responseStatus

Scenario: pathMatches('/stripe/v1/charges') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/stripe-mock.feature@stripe_charges_post') { tokens: stripeState.tokens, charges: stripeState.charges }
  * if (result && result.tokens) karate.set('stripeState.tokens', result.tokens)
  * if (result && result.charges) karate.set('stripeState.charges', result.charges)
  * def response = result.response
  * def responseStatus = result.responseStatus

Scenario: pathMatches('/stripe/v1/charges/{id}') && methodIs('get')
  * def result = call read('classpath:mocks/payment-gateways/stripe-mock.feature@stripe_charges_get') { tokens: stripeState.tokens, charges: stripeState.charges }
  * if (result && result.tokens) karate.set('stripeState.tokens', result.tokens)
  * if (result && result.charges) karate.set('stripeState.charges', result.charges)
  * def response = result.response
  * def responseStatus = result.responseStatus

Scenario: pathMatches('/stripe/v1/refunds') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/stripe-mock.feature@stripe_refunds_post') { tokens: stripeState.tokens, charges: stripeState.charges }
  * if (result && result.tokens) karate.set('stripeState.tokens', result.tokens)
  * if (result && result.charges) karate.set('stripeState.charges', result.charges)
  * def response = result.response
  * def responseStatus = result.responseStatus

Scenario: pathMatches('/stripe/v1/') && headerContains('Authorization', 'Bearer invalid')
  * def result = call read('classpath:mocks/payment-gateways/stripe-mock.feature@stripe_invalid_key') { tokens: stripeState.tokens, charges: stripeState.charges }
  * def response = result.response
  * def responseStatus = result.responseStatus

# PayPal gateway - OAuth Token
Scenario: pathMatches('/paypal/v1/oauth2/token') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/paypal-mock.feature@paypal_oauth_post') { orders: paypalState.orders, captures: paypalState.captures }
  * if (result && result.orders) karate.set('paypalState.orders', result.orders)
  * if (result && result.captures) karate.set('paypalState.captures', result.captures)
  * def response = result.response
  * def responseStatus = result.responseStatus

# PayPal gateway - Create Order
Scenario: pathMatches('/paypal/v2/checkout/orders') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/paypal-mock.feature@paypal_orders_post') { orders: paypalState.orders, captures: paypalState.captures }
  * if (result && result.orders) karate.set('paypalState.orders', result.orders)
  * if (result && result.captures) karate.set('paypalState.captures', result.captures)
  * def response = result.response
  * def responseStatus = result.responseStatus

# PayPal gateway - Capture Order
Scenario: pathMatches('/paypal/v2/checkout/orders/{id}/capture') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/paypal-mock.feature@paypal_orders_capture_post') { orders: paypalState.orders, captures: paypalState.captures }
  * if (result && result.orders) karate.set('paypalState.orders', result.orders)
  * if (result && result.captures) karate.set('paypalState.captures', result.captures)
  * def response = result.response
  * def responseStatus = result.responseStatus

# PayPal gateway - Get Order
Scenario: pathMatches('/paypal/v2/checkout/orders/{id}') && methodIs('get')
  * def result = call read('classpath:mocks/payment-gateways/paypal-mock.feature@paypal_orders_get') { orders: paypalState.orders, captures: paypalState.captures }
  * if (result && result.orders) karate.set('paypalState.orders', result.orders)
  * if (result && result.captures) karate.set('paypalState.captures', result.captures)
  * def response = result.response
  * def responseStatus = result.responseStatus

# PayPal gateway - Refund Capture
Scenario: pathMatches('/paypal/v2/payments/captures/{id}/refund') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/paypal-mock.feature@paypal_refund_post') { orders: paypalState.orders, captures: paypalState.captures }
  * if (result && result.orders) karate.set('paypalState.orders', result.orders)
  * if (result && result.captures) karate.set('paypalState.captures', result.captures)
  * def response = result.response
  * def responseStatus = result.responseStatus

# PayPal gateway - Auth Error
Scenario: pathMatches('/paypal/v2/') && !headerContains('Authorization', 'Bearer')
  * def result = call read('classpath:mocks/payment-gateways/paypal-mock.feature@paypal_auth_error') { orders: paypalState.orders, captures: paypalState.captures }
  * def response = result.response
  * def responseStatus = result.responseStatus

# Bank Transfer gateway - Initiate Transfer
Scenario: pathMatches('/bank-transfer/api/v1/transfers') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/bank-transfer-mock.feature@bt_transfers_post') { transfers: bankTransferState.transfers }
  * if (result && result.transfers) karate.set('bankTransferState.transfers', result.transfers)
  * def response = result.response
  * def responseStatus = result.responseStatus

# Bank Transfer gateway - Get Transfer Status
Scenario: pathMatches('/bank-transfer/api/v1/transfers/{id}') && methodIs('get')
  * def result = call read('classpath:mocks/payment-gateways/bank-transfer-mock.feature@bt_transfers_get') { transfers: bankTransferState.transfers }
  * if (result && result.transfers) karate.set('bankTransferState.transfers', result.transfers)
  * def response = result.response
  * def responseStatus = result.responseStatus

# Bank Transfer gateway - Verify Account
Scenario: pathMatches('/bank-transfer/api/v1/accounts/verify') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/bank-transfer-mock.feature@bt_accounts_verify_post') { transfers: bankTransferState.transfers }
  * if (result && result.transfers) karate.set('bankTransferState.transfers', result.transfers)
  * def response = result.response
  * def responseStatus = result.responseStatus

# Bank Transfer gateway - Cancel Transfer
Scenario: pathMatches('/bank-transfer/api/v1/transfers/{id}/cancel') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/bank-transfer-mock.feature@bt_transfers_cancel_post') { transfers: bankTransferState.transfers }
  * if (result && result.transfers) karate.set('bankTransferState.transfers', result.transfers)
  * def response = result.response
  * def responseStatus = result.responseStatus

# Bank Transfer gateway - Refund Transfer
Scenario: pathMatches('/bank-transfer/api/v1/transfers/{id}/refund') && methodIs('post')
  * def result = call read('classpath:mocks/payment-gateways/bank-transfer-mock.feature@bt_transfers_refund_post') { transfers: bankTransferState.transfers }
  * if (result && result.transfers) karate.set('bankTransferState.transfers', result.transfers)
  * def response = result.response
  * def responseStatus = result.responseStatus

# Bank Transfer gateway - List Transfers
Scenario: pathMatches('/bank-transfer/api/v1/transfers') && methodIs('get')
  * def result = call read('classpath:mocks/payment-gateways/bank-transfer-mock.feature@bt_transfers_list_get') { transfers: bankTransferState.transfers }
  * if (result && result.transfers) karate.set('bankTransferState.transfers', result.transfers)
  * def response = result.response
  * def responseStatus = result.responseStatus

# Bank Transfer gateway - Auth Error
Scenario: pathMatches('/bank-transfer/api/v1/') && !headerContains('Authorization', 'Bearer')
  * def result = call read('classpath:mocks/payment-gateways/bank-transfer-mock.feature@bt_auth_error') { transfers: bankTransferState.transfers }
  * def response = result.response
  * def responseStatus = result.responseStatus

# OAuth token
Scenario: pathMatches('/auth/oauth/token') && methodIs('post')
  * def response = { access_token: uuid(), token_type: 'bearer', expires_in: 3600 }
  * def responseStatus = 200

# Auth login
Scenario: pathMatches('/api/v1/auth/login') && methodIs('post')
  * print 'Mock server: handling POST /api/v1/auth/login'
  * def token = 'mock_' + java.util.UUID.randomUUID()
  * def ts = new java.util.Date().toString()
  * print 'Generated mock token:', token, 'ts:', ts
  * def response = { success: true, message: 'Login successful', data: { token: '#(token)' }, timestamp: '#(ts)', path: '/api/v1/auth/login' }
  * def responseStatus = 200

# Delegate to user-service mock
Scenario: pathMatches('/users') && methodIs('post')
  * def now = new java.util.Date().toString()
  * def response = { id: 1, name: request.name, email: request.email, createdAt: now, metadata: { version: '1.0', source: 'mock' } }
  * def responseStatus = 201

Scenario: pathMatches('/users/{id}') && methodIs('get')
  * def id = pathParams.id
  * def response = id == '1' ? { id: 1, name: 'John Doe', email: 'john@example.com', createdAt: new java.util.Date().toString(), metadata: { version: '1.0', source: 'mock' } } : { error: { code: 'NOT_FOUND', message: 'User not found' } }
  * def responseStatus = id == '1' ? 200 : 404

Scenario: pathMatches('/users/1/profile') && methodIs('get')
  * def response = { id: 1, profile: { nickname: 'jdoe', preferences: {} } }
  * def responseStatus = 200

# Order service
Scenario: pathMatches('/orders') && methodIs('post')
  * def response = { id: 1, status: 'CREATED', userId: request.userId, sku: request.sku, quantity: request.quantity }
  * def responseStatus = 201

Scenario: pathMatches('/orders/{id}') && methodIs('get')
  * def id = pathParams.id
  * def response = id == '1' ? { id: 1, status: 'CREATED', userId: 1, sku: 'ABC-123', quantity: 1 } : { error: { code: 'NOT_FOUND', message: 'Order not found' } }
  * def responseStatus = id == '1' ? 200 : 404

# External dependencies examples
Scenario: pathMatches('/api/external/users/{id}') && methodIs('get')
  * def body = read('classpath:mocks/mock-responses/user-responses.json')
  * def response = body
  * def responseStatus = 200

Scenario: pathMatches('/api/external/payments') && methodIs('post')
  * def response = { transactionId: '#(uuid())', status: 'SUCCESS' }
  * def responseStatus = 200

# ==== Payments API mocks ====

# Create Payment Request
Scenario: pathMatches('/api/v1/payments/requests') && methodIs('post')
  * def reqId = uuid()
  * def token = 'tok_' + uuid()
  * def now = new java.util.Date().toISOString ? new java.util.Date().toISOString() : new java.util.Date().toString()
  * def allowed = request.allowedPaymentMethods ? request.allowedPaymentMethods : ['CREDIT_CARD']
  * def preMethod = request.preSelectedPaymentMethod ? request.preSelectedPaymentMethod : 'CREDIT_CARD'
  * def reqCode = 'REQ-' + reqId.substring(0,8)
  * def paymentRequest = { id: reqId, requestCode: reqCode, paymentToken: token, title: request.title, amount: request.amount, currency: request.currency || 'USD', payerName: request.payerName, payerEmail: request.payerEmail, payerPhone: request.payerPhone, allowedPaymentMethods: allowed, preSelectedPaymentMethod: preMethod, status: 'PENDING', expiresAt: null, paidAt: null, tenantId: request.tenantId || 1, metadata: request.metadata || {}, createdAt: now, updatedAt: now }
  * eval payments[reqId] = paymentRequest
  * def response = paymentRequest
  * def responseStatus = 201

# Get Payment Request by ID
Scenario: pathMatches('/api/v1/payments/requests/{id}') && methodIs('get')
  * def id = pathParams.id
  * def exists = payments[id]
  * if (!exists) karate.set('responseStatus', 404)
  * if (!exists) karate.set('response', { error: { code: 'NOT_FOUND', message: 'Payment request not found' } })
  * if (exists) karate.set('responseStatus', 200)
  * if (exists) karate.set('response', payments[id])

# Process Payment Transaction
Scenario: pathMatches('/api/v1/payments/transactions/process') && methodIs('post')
  * def token = request.paymentToken
  * def reqEntry = function(){ for (var k in payments) { if (payments[k].paymentToken == token) return payments[k]; } return null; }
  * def reqObj = reqEntry()
  * def txnId = uuid()
  * def now = new java.util.Date().toISOString ? new java.util.Date().toISOString() : new java.util.Date().toString()
  * def txnCode = 'TXN-' + txnId.substring(0,8)
  * def extTxnId = 'ext_' + txnId
  * def pmDetails = request.paymentMethodDetails ? request.paymentMethodDetails : { maskedPan: '****1111' }
  * def amountVal = reqObj ? reqObj.amount : (request.amount ? request.amount : 100)
  * def currencyVal = reqObj ? reqObj.currency : (request.currency ? request.currency : 'USD')
  * def metadataVal = request.metadata ? request.metadata : {}
  * def txn = { id: txnId, transactionCode: txnCode, externalTransactionId: extTxnId, paymentRequestId: reqObj ? reqObj.id : null, transactionType: 'PAYMENT', transactionStatus: 'SUCCESS', amount: amountVal, currency: currencyVal, paymentMethod: request.paymentMethod || 'CREDIT_CARD', paymentMethodDetails: pmDetails, gatewayName: request.gatewayName || 'Stripe', gatewayResponse: { message: 'Authorized' }, processedAt: now, errorCode: null, errorMessage: null, metadata: metadataVal, retryCount: 0, maxRetries: 3, createdAt: now, updatedAt: now }
  * eval transactions[txnId] = txn
  * if (reqObj) { reqObj.status = 'COMPLETED'; reqObj.paidAt = now; payments[reqObj.id] = reqObj }
  * def response = { id: '#(txnId)', transactionCode: '#(txnCode)', externalTransactionId: '#(extTxnId)', paymentRequestId: '#(reqObj ? reqObj.id : null)', transactionType: 'PAYMENT', transactionStatus: 'SUCCESS', amount: '#(amountVal)', currency: '#(currencyVal)', paymentMethod: '#(request.paymentMethod || "CREDIT_CARD")', paymentMethodDetails: '#(pmDetails)', gatewayName: '#(request.gatewayName || "Stripe")', gatewayResponse: { message: 'Authorized' }, processedAt: '#(now)', errorCode: null, errorMessage: null, metadata: '#(metadataVal)', retryCount: 0, maxRetries: 3, createdAt: '#(now)', updatedAt: '#(now)' }
  * def responseStatus = 201