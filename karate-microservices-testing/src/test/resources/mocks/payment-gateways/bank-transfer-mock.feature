Feature: Bank Transfer Payment Gateway Mock Server

Background:
  * def uuid = function(){ return java.util.UUID.randomUUID() + '' }
  * def generateTransferId = function(){ return 'BT-' + java.util.UUID.randomUUID().toString().replace('-', '').substring(0, 20).toUpperCase() }
  * def generateRefNumber = function(){ return 'REF-' + Math.floor(Math.random() * 1000000000000) }
  * def transfers = karate.get('transfers') ? transfers : {}

# Bank Transfer - Initiate Transfer Request
@bt_transfers_post
Scenario: pathMatches('/bank-transfer/api/v1/transfers') && methodIs('post')
  * def transferId = generateTransferId()
  * def refNumber = generateRefNumber()
  * def now = new Date().toISOString()
  * def amount = request.amount || 100.00
  * def currency = request.currency || 'USD'
  * def accountNumber = request.accountNumber || '****1234'

  # Build transfer data object
  * def transferData = {}
  * set transferData.id = transferId
  * set transferData.referenceNumber = refNumber
  * set transferData.status = 'PENDING'
  * set transferData.amount = amount
  * set transferData.currency = currency
  * set transferData.fromAccount = request.fromAccount || 'MERCHANT-ACCOUNT'
  * set transferData.toAccount = accountNumber
  * set transferData.bankName = request.bankName || 'Test Bank'
  * set transferData.routingNumber = request.routingNumber || '021000021'
  * set transferData.accountHolderName = request.accountHolderName || 'John Doe'
  * set transferData.createdAt = now
  * set transferData.estimatedCompletionDate = now
  * set transferData.instructions = 'Transfer will be processed within 1-3 business days'
  * eval transfers[transferId] = transferData

  # Build response object
  * def response = {}
  * set response.id = transferId
  * set response.status = 'PENDING'
  * set response.amount = amount
  * set response.currency = currency
  * set response.success = true
  * set response.externalTransactionId = refNumber
  * set response.message = 'Bank transfer initiated successfully'
  * def responseStatus = 201

# Bank Transfer - Check Transfer Status
@bt_transfers_get
Scenario: pathMatches('/bank-transfer/api/v1/transfers/{id}') && methodIs('get')
  * def id = pathParams.id
  * def transfer = transfers[id]

  # Simulate status progression: PENDING -> PROCESSING -> COMPLETED
  * def statusMap = { 'PENDING': 'PROCESSING', 'PROCESSING': 'COMPLETED', 'COMPLETED': 'COMPLETED' }
  * if (transfer && statusMap[transfer.status]) transfer.status = statusMap[transfer.status]

  * def response = transfer ? { id: transfer.id, referenceNumber: transfer.referenceNumber, status: transfer.status, amount: transfer.amount, currency: transfer.currency, bankName: transfer.bankName, accountHolderName: transfer.accountHolderName, createdAt: transfer.createdAt, completedAt: transfer.status == 'COMPLETED' ? new Date().toISOString() : null } : { error: { code: 'NOT_FOUND', message: 'Transfer not found: ' + id } }
  * def responseStatus = transfer ? 200 : 404

# Bank Transfer - Verify Account
@bt_accounts_verify_post
Scenario: pathMatches('/bank-transfer/api/v1/accounts/verify') && methodIs('post')
  * def accountNumber = request.accountNumber
  * def routingNumber = request.routingNumber

  # Simple validation: reject if account number is less than 8 digits
  * def isValid = accountNumber && accountNumber.length >= 8

  * def response = isValid ? { valid: true, accountNumber: accountNumber, routingNumber: routingNumber, bankName: 'Test Bank', accountType: 'CHECKING', accountHolderName: 'John Doe' } : { valid: false, error: { code: 'INVALID_ACCOUNT', message: 'Invalid account number or routing number' } }
  * def responseStatus = isValid ? 200 : 400

# Bank Transfer - Cancel Transfer
@bt_transfers_cancel_post
Scenario: pathMatches('/bank-transfer/api/v1/transfers/{id}/cancel') && methodIs('post')
  * def id = pathParams.id
  * def transfer = transfers[id]
  * def canCancel = transfer && (transfer.status == 'PENDING' || transfer.status == 'PROCESSING')

  * if (canCancel) transfer.status = 'CANCELLED'
  * def response = canCancel ? { id: id, status: 'CANCELLED', message: 'Transfer cancelled successfully' } : { error: { code: 'CANNOT_CANCEL', message: 'Transfer cannot be cancelled in current state' } }
  * def responseStatus = canCancel ? 200 : 400

# Bank Transfer - Refund Transfer
@bt_transfers_refund_post
Scenario: pathMatches('/bank-transfer/api/v1/transfers/{id}/refund') && methodIs('post')
  * def id = pathParams.id
  * def transfer = transfers[id]
  * def refundId = 'REFUND-' + generateTransferId()
  * def now = new Date().toISOString()
  * def amount = request.amount || (transfer ? transfer.amount : 100.00)
  * def currency = request.currency || (transfer ? transfer.currency : 'USD')
  * def canRefund = transfer && transfer.status == 'COMPLETED'

  # Build refund response
  * def successResponse = { id: '#(refundId)', status: 'REFUNDED', amount: #(amount), currency: '#(currency)', success: true, externalRefundId: '#(refundId)', message: 'Bank transfer refund processed successfully' }
  * def errorResponse = { error: { code: 'CANNOT_REFUND', message: 'Transfer cannot be refunded in current state' } }
  * def response = canRefund ? successResponse : errorResponse
  * def responseStatus = canRefund ? 200 : 400

# Bank Transfer - List Transfers
@bt_transfers_list_get
Scenario: pathMatches('/bank-transfer/api/v1/transfers') && methodIs('get')
  * def allTransfers = []
  * def addTransfer = function(k){ allTransfers.push(transfers[k]) }
  * eval Object.keys(transfers).forEach(addTransfer)
  * def response = { transfers: allTransfers, total: allTransfers.length }
  * def responseStatus = 200

# Bank Transfer API Error - Unauthorized (401)
@bt_auth_error
Scenario: pathMatches('/bank-transfer/api/v1/') && !headerContains('Authorization', 'Bearer')
  * def response = { error: { code: 'UNAUTHORIZED', message: 'Missing or invalid authentication token' } }
  * def responseStatus = 401
