Feature: Stripe Payment Gateway Mock Server

Background:
  * def uuid = function(){ return java.util.UUID.randomUUID() + '' }
  * def generateToken = function(){ return 'tok_' + java.util.UUID.randomUUID().toString().replace('-', '').substring(0, 24) }
  * def generateChargeId = function(){ return 'ch_' + java.util.UUID.randomUUID().toString().replace('-', '').substring(0, 24) }
  * def tokens = karate.get('tokens') ? tokens : {}
  * def charges = karate.get('charges') ? charges : {}

# Stripe Tokenization API - Create Card Token
@stripe_tokens_post
Scenario: pathMatches('/stripe/v1/tokens') && methodIs('post')
  * def tokenId = generateToken()
  * def now = Math.floor(Date.now() / 1000)
  * def cardLast4 = request.cardNumber ? request.cardNumber.substring(request.cardNumber.length - 4) : '4242'
  * def cardBrand = request.cardNumber && request.cardNumber.startsWith('5') ? 'mastercard' : 'visa'
  * def cardId = 'card_' + uuid().substring(0, 24)
  * def expMonthVal = parseInt(request.expiryMonth || '12')
  * def expYearVal = parseInt(request.expiryYear || '2025')
  * def fingerprintVal = uuid().substring(0, 16)

  # Success response
  * def tokenData = { id: tokenId, object: 'token', type: 'card', card: { id: cardId, object: 'card', brand: cardBrand, last4: cardLast4, exp_month: expMonthVal, exp_year: expYearVal, fingerprint: fingerprintVal, country: 'US', name: request.cardHolderName || 'Test User', funding: 'credit' }, created: now, livemode: false, used: false }
  * eval tokens[tokenId] = tokenData
  * def response = { token: '#(tokenId)', tokenType: 'card', success: true }
  * def responseStatus = 200

# Stripe Charges API - Create Charge (Process Payment)
@stripe_charges_post
Scenario: pathMatches('/stripe/v1/charges') && methodIs('post')
  * def chargeId = generateChargeId()
  * def now = Math.floor(Date.now() / 1000)
  * def amount = request.amount ? (request.amount * 100) : 10000
  * def currency = request.currency ? request.currency.toLowerCase() : 'usd'
  * def token = request.token || request.source
  * def balanceTxn = 'txn_' + uuid().substring(0, 24)
  * def sourceCardId = 'card_' + uuid().substring(0, 24)
  * def receiptUrl = 'https://pay.stripe.com/receipts/test_' + chargeId
  * def descriptionVal = request.description ? request.description : 'Payment charge'
  * def responseAmount = request.amount ? request.amount : 100
  * def responseCurrency = request.currency ? request.currency : 'USD'

  # Check if token exists (optional validation)
  * def tokenExists = tokens[token] != null

  # Success response
  * def chargeData = { id: chargeId, object: 'charge', amount: amount, amount_captured: amount, amount_refunded: 0, balance_transaction: balanceTxn, captured: true, created: now, currency: currency, description: descriptionVal, paid: true, refunded: false, status: 'succeeded', source: { id: sourceCardId, object: 'card', brand: 'visa', last4: '4242', exp_month: 12, exp_year: 2025 }, receipt_url: receiptUrl }
  * eval charges[chargeId] = chargeData
  * def response = { id: '#(chargeId)', status: 'AUTHORIZED', amount: '#(responseAmount)', currency: '#(responseCurrency)', success: true }
  * def responseStatus = 200

# Stripe Charges API - Retrieve Charge
@stripe_charges_get
Scenario: pathMatches('/stripe/v1/charges/{id}') && methodIs('get')
  * def id = pathParams.id
  * def charge = charges[id]
  * def response = charge ? charge : { error: { type: 'invalid_request_error', message: 'No such charge: ' + id, param: 'id' } }
  * def responseStatus = charge ? 200 : 404

# Stripe Refunds API - Create Refund
@stripe_refunds_post
Scenario: pathMatches('/stripe/v1/refunds') && methodIs('post')
  * def refundId = 're_' + uuid().substring(0, 24)
  * def chargeId = request.charge
  * def charge = charges[chargeId]
  * def now = Math.floor(Date.now() / 1000)
  * def amount = request.amount || (charge ? charge.amount : 10000)
  * def refundAmount = amount / 100
  * def refundCurrency = charge ? charge.currency.toUpperCase() : 'USD'

  # Success response
  * def refundData = { id: refundId, object: 'refund', amount: amount, charge: chargeId, created: now, currency: charge ? charge.currency : 'usd', status: 'succeeded' }
  * def response = { id: '#(refundId)', status: 'succeeded', amount: #(refundAmount), currency: '#(refundCurrency)', success: true }
  * def responseStatus = 200

# Stripe API Error - Invalid API Key (401)
@stripe_invalid_key
Scenario: pathMatches('/stripe/v1/') && headerContains('Authorization', 'Bearer invalid')
  * def response = { error: { type: 'invalid_request_error', message: 'Invalid API Key provided', code: 'api_key_invalid' } }
  * def responseStatus = 401
