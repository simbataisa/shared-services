# Payment Method Types Reference

This document explains all supported payment method types in the system and how to use them in data-driven tests.

## Payment Method Type Enum

Based on `backend/src/main/java/com/ahss/enums/PaymentMethodType.java`, the system supports the following payment methods:

| Enum Value | Display Name | Category | Processing Type |
|------------|--------------|----------|-----------------|
| `CREDIT_CARD` | Credit Card | Generic | Online, Instant |
| `DEBIT_CARD` | Debit Card | Generic | Online, Instant |
| `BANK_TRANSFER` | Bank Transfer | Generic | Offline, Manual Verification |
| `DIGITAL_WALLET` | Digital Wallet | Generic | Online, Instant |
| `PAYPAL` | PayPal | Gateway-Specific | Online, Instant |
| `STRIPE` | Stripe | Gateway-Specific | Online, Instant |
| `MANUAL` | Manual Payment | Offline | Manual Verification |

## Payment Method Categories

### 1. Generic Payment Methods

These are payment method **types** that can be processed through different gateways:

- **CREDIT_CARD** - Credit card payments (can use Stripe, PayPal, or other gateways)
- **DEBIT_CARD** - Debit card payments (can use Stripe or other gateways)
- **BANK_TRANSFER** - Bank transfer/ACH payments
- **DIGITAL_WALLET** - Digital wallet payments (PayPal, Apple Pay, Google Pay, etc.)

### 2. Gateway-Specific Payment Methods

These specify both the payment type AND the gateway:

- **STRIPE** - Payments processed directly through Stripe gateway
- **PAYPAL** - Payments processed directly through PayPal gateway

### 3. Offline Payment Methods

- **MANUAL** - Manual payment processing (invoices, checks, etc.)

## Processing Characteristics

### Online Processing Methods
Requires online processing and support instant settlement:
- CREDIT_CARD
- DEBIT_CARD
- DIGITAL_WALLET
- PAYPAL
- STRIPE

### Manual Verification Methods
Requires manual verification and delayed settlement:
- BANK_TRANSFER
- MANUAL

## allowedPaymentMethods Usage

The `allowedPaymentMethods` field in test data specifies which payment methods are **allowed** for a payment request. It's an array that can include:

### Scenario 1: Single Generic Method
Allow only credit cards (any gateway can process):
```json
"allowedPaymentMethods": ["CREDIT_CARD"]
```

### Scenario 2: Single Gateway-Specific Method
Allow only Stripe payments:
```json
"allowedPaymentMethods": ["STRIPE"]
```

### Scenario 3: Generic + Gateway-Specific
Allow credit cards AND specify Stripe as available gateway:
```json
"allowedPaymentMethods": ["CREDIT_CARD", "STRIPE"]
```

This means:
- Credit card payments are allowed
- Stripe is an available gateway for processing

### Scenario 4: Multiple Payment Methods
Allow multiple payment types:
```json
"allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER"]
```

User can choose any of these methods.

### Scenario 5: Multiple Methods + Gateways
Allow multiple payment types with specific gateways:
```json
"allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "STRIPE", "PAYPAL"]
```

This gives users maximum flexibility:
- Can pay with credit or debit card
- Can choose Stripe or PayPal as gateway

## Test Scenario Examples

### Example 1: Stripe Credit Card (Standard)
```json
{
  "testCase": "stripe_credit_card_visa",
  "paymentMethod": "CREDIT_CARD",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["CREDIT_CARD", "STRIPE"],
  "paymentMethodDetails": {
    "stripeToken": "tok_visa"
  },
  "expectedStatus": "COMPLETED"
}
```

**Explanation:**
- User selects **CREDIT_CARD** as payment method
- Payment is processed through **Stripe** gateway
- Both CREDIT_CARD and STRIPE are in allowed methods

### Example 2: Direct Stripe Payment
```json
{
  "testCase": "stripe_only_payment",
  "paymentMethod": "STRIPE",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["STRIPE"],
  "paymentMethodDetails": {
    "stripeToken": "tok_amex"
  },
  "expectedStatus": "COMPLETED"
}
```

**Explanation:**
- User selects **STRIPE** directly (gateway-specific method)
- Only Stripe payments are allowed

### Example 3: PayPal Digital Wallet
```json
{
  "testCase": "paypal_digital_wallet",
  "paymentMethod": "DIGITAL_WALLET",
  "gateway": "PayPal",
  "allowedPaymentMethods": ["DIGITAL_WALLET", "PAYPAL"],
  "paymentMethodDetails": {
    "paypalOrderId": "ORDER456",
    "walletType": "PAYPAL"
  },
  "expectedStatus": "COMPLETED"
}
```

**Explanation:**
- User selects **DIGITAL_WALLET** as payment method
- Payment is processed through **PayPal** gateway
- Both generic and gateway-specific methods allowed

### Example 4: Bank Transfer (Manual Verification)
```json
{
  "testCase": "bank_transfer_checking",
  "paymentMethod": "BANK_TRANSFER",
  "gateway": "BankTransfer",
  "allowedPaymentMethods": ["BANK_TRANSFER"],
  "paymentMethodDetails": {
    "accountNumber": "1234567890",
    "routingNumber": "987654321",
    "accountHolderName": "John Doe",
    "accountType": "CHECKING"
  },
  "expectedStatus": "COMPLETED"
}
```

**Explanation:**
- Bank transfers require manual verification
- No online processing or instant settlement
- Payment marked COMPLETED after verification

### Example 5: Multiple Allowed Methods
```json
{
  "testCase": "multi_method_credit_card_stripe",
  "paymentMethod": "CREDIT_CARD",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "STRIPE", "PAYPAL"],
  "paymentMethodDetails": {
    "stripeToken": "tok_visa"
  },
  "expectedStatus": "COMPLETED"
}
```

**Explanation:**
- User has 4 payment options to choose from
- User chooses **CREDIT_CARD** via **Stripe**
- This simulates a flexible payment request

## Common Test Patterns

### Pattern 1: Gateway-Specific Tests
Test a specific gateway's payment processing:

```json
{
  "paymentMethod": "STRIPE",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["STRIPE"]
}
```

### Pattern 2: Generic Method with Gateway
Test a generic payment method through a specific gateway:

```json
{
  "paymentMethod": "CREDIT_CARD",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["CREDIT_CARD", "STRIPE"]
}
```

### Pattern 3: Multiple Options (User Choice)
Test scenarios where users can choose from multiple methods:

```json
{
  "paymentMethod": "CREDIT_CARD",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER"]
}
```

### Pattern 4: Cross-Gateway Testing
Test the same payment method across different gateways:

**Stripe:**
```json
{
  "paymentMethod": "CREDIT_CARD",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["CREDIT_CARD", "STRIPE"]
}
```

**PayPal:**
```json
{
  "paymentMethod": "CREDIT_CARD",
  "gateway": "PayPal",
  "allowedPaymentMethods": ["CREDIT_CARD", "PAYPAL"]
}
```

## Available Test Scenarios

The current `payment-scenarios.json` includes:

1. ✅ **CREDIT_CARD** via Stripe (Visa, Mastercard)
2. ✅ **DEBIT_CARD** via Stripe
3. ✅ **CREDIT_CARD** via PayPal
4. ✅ **DIGITAL_WALLET** via PayPal
5. ✅ **BANK_TRANSFER** (Checking, Savings)
6. ✅ **STRIPE** direct (gateway-specific)
7. ✅ **PAYPAL** direct (gateway-specific)
8. ✅ **Multiple allowed methods** scenarios

## Adding New Payment Method Scenarios

### To add MANUAL payment:
```json
{
  "testCase": "manual_payment_invoice",
  "paymentMethod": "MANUAL",
  "gateway": "Manual",
  "allowedPaymentMethods": ["MANUAL"],
  "paymentMethodDetails": {
    "invoiceNumber": "INV-001",
    "paymentReference": "REF-12345"
  },
  "expectedStatus": "PENDING",
  "description": "Manual payment via invoice"
}
```

### To add Digital Wallet via Stripe:
```json
{
  "testCase": "stripe_apple_pay",
  "paymentMethod": "DIGITAL_WALLET",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["DIGITAL_WALLET", "STRIPE"],
  "paymentMethodDetails": {
    "stripeToken": "tok_applepay",
    "walletType": "APPLE_PAY"
  },
  "expectedStatus": "COMPLETED",
  "description": "Apple Pay payment through Stripe"
}
```

## Best Practices

1. **Always include both generic and gateway-specific methods** when testing gateway processing:
   ```json
   "allowedPaymentMethods": ["CREDIT_CARD", "STRIPE"]
   ```

2. **Use gateway-specific methods alone** when testing gateway-specific features:
   ```json
   "allowedPaymentMethods": ["STRIPE"]
   ```

3. **Test multiple allowed methods** to verify user choice scenarios:
   ```json
   "allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER"]
   ```

4. **Group test cases by payment method type** for better organization

5. **Include descriptive test case names** that indicate method + gateway + card type

## Summary

- **7 payment method types** available in the system
- **allowedPaymentMethods** can mix generic and gateway-specific methods
- **Generic methods** (CREDIT_CARD, DEBIT_CARD) can be processed by multiple gateways
- **Gateway-specific methods** (STRIPE, PAYPAL) lock to one gateway
- **Test scenarios** should cover single methods, multiple methods, and cross-gateway scenarios
