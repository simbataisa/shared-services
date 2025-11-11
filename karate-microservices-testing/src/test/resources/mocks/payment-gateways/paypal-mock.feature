Feature: PayPal Payment Gateway Mock Server

Background:
  * def uuid = function(){ return java.util.UUID.randomUUID() + '' }
  * def generateOrderId = function(){ return 'PAYPAL-ORDER-' + java.util.UUID.randomUUID().toString().replace('-', '').substring(0, 17).toUpperCase() }
  * def generateAuthId = function(){ return 'AUTH-' + java.util.UUID.randomUUID().toString().replace('-', '').substring(0, 17).toUpperCase() }
  * def generateCaptureId = function(){ return 'CAPTURE-' + java.util.UUID.randomUUID().toString().replace('-', '').substring(0, 17).toUpperCase() }
  * def orders = karate.get('orders') ? orders : {}
  * def captures = karate.get('captures') ? captures : {}

# PayPal OAuth 2.0 Token API
@paypal_oauth_post
Scenario: pathMatches('/paypal/v1/oauth2/token') && methodIs('post')
  * def accessToken = 'A21AAL' + uuid().replace('-', '').substring(0, 30)
  * def now = Math.floor(Date.now() / 1000)
  * def response = { scope: 'https://uri.paypal.com/services/invoicing https://uri.paypal.com/services/vault/payment-tokens/read', access_token: accessToken, token_type: 'Bearer', app_id: 'APP-80W284485P519543T', expires_in: 32400, nonce: now }
  * def responseStatus = 200

# PayPal Create Order API
@paypal_orders_post
Scenario: pathMatches('/paypal/v2/checkout/orders') && methodIs('post')
  * def orderId = generateOrderId()
  * def now = new Date().toISOString()
  * def amount = request.purchase_units[0].amount
  * def currency = amount.currency_code || 'USD'
  * def value = amount.value

  # Build response object step by step
  * def orderData = {}
  * set orderData.id = orderId
  * set orderData.status = 'CREATED'
  * set orderData.intent = request.intent || 'CAPTURE'
  * set orderData.purchase_units = []
  * set orderData.purchase_units[0] = { reference_id: '#(request.purchase_units[0].reference_id || "default")', amount: { currency_code: '#(currency)', value: '#(value)' } }
  * set orderData.create_time = now
  * set orderData.links = []
  * set orderData.links[0] = { href: '#("https://api.sandbox.paypal.com/v2/checkout/orders/" + orderId)', rel: 'self', method: 'GET' }
  * set orderData.links[1] = { href: '#("https://www.sandbox.paypal.com/checkoutnow?token=" + orderId)', rel: 'approve', method: 'GET' }
  * set orderData.links[2] = { href: '#("https://api.sandbox.paypal.com/v2/checkout/orders/" + orderId + "/capture")', rel: 'capture', method: 'POST' }

  * eval orders[orderId] = orderData
  * def response = orderData
  * def responseStatus = 201

# PayPal Capture Order (Authorize Payment)
@paypal_orders_capture_post
Scenario: pathMatches('/paypal/v2/checkout/orders/{id}/capture') && methodIs('post')
  * def id = pathParams.id
  * def order = orders[id]
  * def captureId = generateCaptureId()
  * def now = new Date().toISOString()

  * def captureData = { id: captureId, status: 'COMPLETED', amount: order.purchase_units[0].amount, final_capture: true, seller_protection: { status: 'ELIGIBLE' }, create_time: now, update_time: now }
  * eval captures[captureId] = captureData

  # Update order status
  * if (order) order.status = 'COMPLETED'
  * if (order) order.purchase_units[0].payments = { captures: [captureData] }

  * def response = order ? { id: id, status: 'AUTHORIZED', amount: parseFloat(order.purchase_units[0].amount.value), currency: order.purchase_units[0].amount.currency_code, success: true, externalTransactionId: captureId } : { error: { type: 'RESOURCE_NOT_FOUND', message: 'Order not found: ' + id } }
  * def responseStatus = order ? 200 : 404

# PayPal Get Order Details
@paypal_orders_get
Scenario: pathMatches('/paypal/v2/checkout/orders/{id}') && methodIs('get')
  * def id = pathParams.id
  * def order = orders[id]
  * def response = order ? order : { name: 'RESOURCE_NOT_FOUND', message: 'The specified resource does not exist.', debug_id: uuid().substring(0, 13), details: [{ issue: 'INVALID_RESOURCE_ID', description: 'Specified resource ID does not exist.' }] }
  * def responseStatus = order ? 200 : 404

# PayPal Refund Capture
@paypal_refund_post
Scenario: pathMatches('/paypal/v2/payments/captures/{id}/refund') && methodIs('post')
  * def id = pathParams.id
  * def capture = captures[id]
  * def refundId = 'REFUND-' + uuid().substring(0, 17).toUpperCase()
  * def now = new Date().toISOString()
  * def amount = request.amount || (capture ? capture.amount : { currency_code: 'USD', value: '100.00' })

  * def refundData = { id: refundId, status: 'COMPLETED', amount: amount, seller_payable_breakdown: { total_refunded_amount: amount }, create_time: now, update_time: now }
  * def response = { id: '#(refundId)', status: 'COMPLETED', amount: #(amount) }
  * def responseStatus = 201

# PayPal API Error - Unauthorized (401)
@paypal_auth_error
Scenario: pathMatches('/paypal/v2/') && !headerContains('Authorization', 'Bearer')
  * def response = { error: 'invalid_client', error_description: 'Client Authentication failed' }
  * def responseStatus = 401
